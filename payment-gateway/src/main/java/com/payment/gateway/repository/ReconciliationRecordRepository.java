package com.payment.gateway.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.payment.gateway.model.ReconciliationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReconciliationRecordRepository extends BaseMapper<ReconciliationRecord> {

    @Select("SELECT * FROM reconciliation_record WHERE batch_no = #{batchNo}")
    List<ReconciliationRecord> findByBatchNo(@Param("batchNo") String batchNo);

    @Select("""
        SELECT * FROM reconciliation_record
        WHERE diff_type != 'MATCH'
        ORDER BY created_at DESC
        LIMIT #{limit}
    """)
    List<ReconciliationRecord> findRecentErrors(@Param("limit") int limit);
}
