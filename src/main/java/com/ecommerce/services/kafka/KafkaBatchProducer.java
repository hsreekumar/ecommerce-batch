package com.ecommerce.services.kafka;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ecommerce.domain.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaBatchProducer {

	@Value("${kafka.topic}")
	private String topic;
	
	@Value("${kafka.server}")
	private String server;
	
	KafkaProducer<String, Order> producer;

	@PostConstruct
	public void initProducer() {
		producer = createKafkaProducer();
	}

	public void submit(Order order) {
		producer.send(new ProducerRecord<>(topic, null, order), new Callback() {
			@Override
			public void onCompletion(RecordMetadata recordMetadata, Exception e) {
				if (e != null) {
					log.error("Something bad happened", e);
				}
			}
		});
	}

	public KafkaProducer<String, Order> createKafkaProducer() {

		// create Producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaMessageSerializer.class.getName());

		// create safe Producer
		properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
		properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); 

		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024)); 

		// create the producer
		KafkaProducer<String, Order> producer = new KafkaProducer<String, Order>(properties);
		return producer;
	}

}
