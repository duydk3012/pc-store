package com.datn.pc_store.modules.goodsreceipt.repository;

import com.datn.pc_store.modules.goodsreceipt.entity.ProductSerial;
import com.datn.pc_store.modules.goodsreceipt.enums.ProductSerialStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductSerialRepository extends JpaRepository<ProductSerial, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select ps from ProductSerial ps where ps.isTerminated = false and ps.serialNumber in :numbers order by ps.id")
    List<ProductSerial> lockActiveBySerialNumbers(Collection<String> numbers);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select ps from ProductSerial ps where ps.id in :ids order by ps.id")
    List<ProductSerial> lockByIds(Collection<Long> ids);

    Optional<ProductSerial> findFirstBySerialNumberAndIsTerminatedFalse(String serialNumber);

    Optional<ProductSerial> findFirstBySerialNumberOrderByCreatedDateDesc(String serialNumber);

    default Optional<ProductSerial> findBySerialNumber(String serialNumber) {
        return findFirstBySerialNumberOrderByCreatedDateDesc(serialNumber);
    }

    List<ProductSerial> findAllBySerialNumber(String serialNumber);

    @Query("SELECT ps.serialNumber FROM ProductSerial ps WHERE ps.serialNumber IN :serialNumbers AND ps.isTerminated = false")
    Set<String> findActiveExistingSerialNumbers(@Param("serialNumbers") Collection<String> serialNumbers);

    @Query("SELECT ps FROM ProductSerial ps WHERE ps.receiptItem.goodsReceipt.id = :goodsReceiptId")
    List<ProductSerial> findAllByGoodsReceiptId(@Param("goodsReceiptId") Long goodsReceiptId);

    List<ProductSerial> findAllByProductIdAndIsTerminatedFalse(Long productId);

    @Query("""
        SELECT ps FROM ProductSerial ps
        WHERE ps.productId = :productId
          AND ps.isTerminated = false
          AND (:status IS NULL OR ps.status = :status)
    """)
    Page<ProductSerial> findActiveSerialsByProduct(
            @Param("productId") Long productId,
            @Param("status") ProductSerialStatus status,
            Pageable pageable
    );

    @Query(value = """
        SELECT ps FROM ProductSerial ps
        LEFT JOIN ps.receiptItem ri
        LEFT JOIN ri.goodsReceipt gr
        LEFT JOIN PurchaseOrder po ON po.id = gr.purchaseOrderId
        WHERE ps.isTerminated = false
          AND (:supplierId IS NULL OR po.supplierId = :supplierId)
          AND (:productId IS NULL OR ps.productId = :productId)
          AND (:status IS NULL OR ps.status = :status)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(ps.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """,
    countQuery = """
        SELECT COUNT(ps) FROM ProductSerial ps
        LEFT JOIN ps.receiptItem ri
        LEFT JOIN ri.goodsReceipt gr
        LEFT JOIN PurchaseOrder po ON po.id = gr.purchaseOrderId
        WHERE ps.isTerminated = false
          AND (:supplierId IS NULL OR po.supplierId = :supplierId)
          AND (:productId IS NULL OR ps.productId = :productId)
          AND (:status IS NULL OR ps.status = :status)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(ps.serialNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<ProductSerial> searchSerials(
            @Param("supplierId") Long supplierId,
            @Param("productId") Long productId,
            @Param("status") ProductSerialStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
