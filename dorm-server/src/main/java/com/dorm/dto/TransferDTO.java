package com.dorm.dto;
import lombok.Data;

@Data
public class TransferDTO { private Long studentId; private Long newBedId; private Long newRoomId; private String reason; }
