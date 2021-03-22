package com.ecommerce.services.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.domain.Order;
import com.ecommerce.services.GatewayClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaOrderConsumer {

	KafkaConsumer<String, Order> consumer;

	@Value("${kafka.topic}")
	private String topic;

	@Value("${kafka.server}")
	private String server;

	private boolean commit;

	private ReentrantLock lock = new ReentrantLock();

	private Map<Long, Order> customerOrderMap = new HashMap<Long, Order>();

	@Autowired
	private GatewayClient gateway;

	private ExecutorService executor = Executors.newFixedThreadPool(5);

	private volatile int recordCount = 0;

	private static final Integer COUNT_THRESHOLD = 50;

	@PostConstruct
	public void initProducer() {
		consumer = createConsumer(topic);
		executor.submit(new Runnable() {

			@Override
			public void run() {
				startTimer();
			}
		});
		executor.submit(new Runnable() {

			@Override
			public void run() {
				// more consumers could be added as per the partitions and requirement
				startConsumer();
			}
		});
	}

	public KafkaConsumer<String, Order> createConsumer(String topic) {

		String groupId = "batch_order";

		// create consumer configs
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				KafkaMessageDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // disable auto commit of offsets

		// create consumer
		KafkaConsumer<String, Order> consumer = new KafkaConsumer<String, Order>(properties);
		consumer.subscribe(Arrays.asList(topic));

		return consumer;

	}

	// Periodically re-enables the commit flag, so that results are committed even
	// if they havent reached the count threshold
	private void startTimer() {
		TimerTask task = new TimerTask() {
			public void run() {
				commit = true;
				System.out.println("Task performed on: " + new Date() + "n" + "Thread's name: "
						+ Thread.currentThread().getName());
			}
		};

		new Timer().schedule(task, 0, 10000);
	}

	//keeps on pooling data and commits on timer trigger or count threshold
	private void startConsumer() {
		while (true) {
			ConsumerRecords<String, Order> records = consumer.poll(Duration.ofMillis(100));
			groupMessages(records);
			recordCount += records.count();
			if (commit || recordCount > COUNT_THRESHOLD) {
				bulkSave();
				log.info("Committing offsets...");
				consumer.commitSync();
				log.info("Offsets have been committed");
			}

		}

	}

	// Saves bulk data. If not acknowledged due to exception, blocks consumer till successful
	private void bulkSave() {
		commit = false;
		if (recordCount > 0) {
			recordCount = 0;
			boolean ack = false;
			while (!ack) {
				
				try {
					ack = gateway.createBulkOrder(new ArrayList(customerOrderMap.values()));
					if(ack) {
						break;
					}
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// try again
				
			}
			lock.lock();
			try {
				customerOrderMap.clear();
			} finally {
				lock.unlock();
			}
		}

	}

	//group the messages by customerId. All the orders for one customer are appended to the same order as items
	private void groupMessages(ConsumerRecords<String, Order> records) {
		for (ConsumerRecord<String, Order> record : records) {
			try {
				if (ObjectUtils.isNotEmpty(record.value()) && ObjectUtils.isNotEmpty(record.value().getCustomer())
						&& ObjectUtils.isNotEmpty(record.value().getCustomer().getId())) {
					lock.lock();
					try {
						Order order = customerOrderMap.putIfAbsent(record.value().getCustomer().getId(),
								record.value());
						if (ObjectUtils.isNotEmpty(order)) {
							order.getItems().addAll(record.value().getItems());
						}
					} catch (Exception e) {
						log.error("Exception :", e);
					} finally {
						lock.unlock();
					}

				}
			} catch (Exception e) {
				log.error("Exception :", e);
			}
		}

	}
}
