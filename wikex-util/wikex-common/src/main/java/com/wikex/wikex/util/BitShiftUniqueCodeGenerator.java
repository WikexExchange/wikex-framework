package com.wikex.wikex.util;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class BitShiftUniqueCodeGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // 26 chữ cái
    private static final long EPOCH_OFFSET = 1577836800000L; // 01/01/2020 00:00:00 UTC (mốc thời gian tùy chỉnh)
    private static final AtomicLong counter = new AtomicLong(0);

    public static String generateUniqueCode() {
        StringBuilder prefix = new StringBuilder(4);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 4; i++) {
            prefix.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        // Lấy epoch time (mili-giây) từ 01/01/2020
        long epochMillis = Instant.now().toEpochMilli() - EPOCH_OFFSET;
        // Lấy 28 bit thấp của epoch time (đủ cho ~8.5 năm kể từ 2020)
        long epochBits = epochMillis & 0xFFFFFFF; // 28 bit

        // Tăng bộ đếm (12 bit, tối đa 4096)
        long uniqueCounter = counter.getAndIncrement() & 0xFFF; // 12 bit

        // Kết hợp epoch và counter
        long combined = (epochBits << 12) | uniqueCounter;
        // Format thành 12 chữ số, thêm 0 bên trái nếu cần
        String numberPart = String.format("%012d", combined);

        // Kết hợp prefix và số
        return prefix + numberPart;
    }
}