package com.fourt.railskylines.service;

import com.fourt.railskylines.domain.Booking;
import com.fourt.railskylines.domain.Ticket;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String recipient, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipient);
        helper.setSubject(subject);
        helper.setText(content, true); // true = HTML content

        mailSender.send(message);
    }

    public void sendBookingConfirmation(Booking booking, List<Ticket> tickets) throws MessagingException {
        String recipient = booking.getContactEmail();
        String subject = "Xác nhận đặt vé thành công";
        String content = "<h2>Xác nhận đặt vé</h2>" +
                "<p>Mã đặt chỗ: <strong>" + booking.getBookingCode() + "</strong></p>" +
                "<h3>Danh sách vé:</h3>" +
                "<ul>" +
                tickets.stream()
                        .map(ticket -> "<li>Mã vé: " + ticket.getTicketCode() +
                                ", Ghế: " + ticket.getSeat().getSeatId() +
                                ", Hành khách: " + ticket.getName() + "</li>")
                        .collect(Collectors.joining()) +
                "</ul>" +
                "<p>Để tra cứu vé, vui lòng sử dụng Mã vé và CCCD tại trang tra cứu.</p>";

        sendEmail(recipient, subject, content);
    }
}