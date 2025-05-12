package com.fourt.railskylines.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fourt.railskylines.util.constant.PaymentStatusEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private long bookingId;

    @Column(name = "booking_code")
    private String bookingCode;

    private Instant date;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatusEnum paymentStatus;

    @Column(name = "total_price")
    private double totalPrice;

    @Column(name = "pay_at")
    private Instant payAt;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "vnp_txn_ref", nullable = true)
    private String vnpTxnRef;

    @Column(name = "payment_type")
    private String paymentType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @OneToMany(mappedBy = "booking")
    private List<Ticket> tickets;

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    @PrePersist
    public void prePersist() {
        if (this.bookingCode == null) {
            this.bookingCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}