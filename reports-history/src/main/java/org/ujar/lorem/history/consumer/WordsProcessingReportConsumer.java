package org.ujar.lorem.history.consumer;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ujar.lorem.history.consumer.dto.ReportDto;
import org.ujar.lorem.history.entity.Report;
import org.ujar.lorem.history.repository.ReportRepository;
import org.ujar.lorem.shared.exception.ConsumerRecordProcessingException;

@Component
@RequiredArgsConstructor
@Slf4j
class WordsProcessingReportConsumer {

  private final ReportRepository repository;

  @KafkaListener(containerFactory = "processingReportKafkaListenerContainerFactory",
                 topics = "${lorem.kafka.topics.words-processed.name}",
                 groupId = "${spring.kafka.consumer.group-id}")
  @Transactional
  public void consume(ConsumerRecord<String, ReportDto> consumerRecord) {
    try {
      log.info("( {} ) Received report, key: {}, value: {}",
          Thread.currentThread().getName(), consumerRecord.key(), consumerRecord.value());
      ReportDto dto = consumerRecord.value();
      final var report = new Report(dto.mostFrequentWord(),
          dto.avgParagraphSize(),
          (int) dto.avgParagraphProcessingTime(),
          (int) dto.totalProcessingTime());
      repository.saveAndFlush(report);
    } catch (Exception e) {
      throw new ConsumerRecordProcessingException("Error processing report data.", e);
    }
  }
}
