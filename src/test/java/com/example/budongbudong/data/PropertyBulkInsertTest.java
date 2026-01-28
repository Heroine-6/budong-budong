package com.example.budongbudong.data;

import com.example.budongbudong.domain.property.enums.PropertyType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class PropertyBulkInsertTest {

    private static final Random R = new Random();
    private static final String[] ADDRESSES = {
            "서울특별시 종로구",
            "서울특별시 중구",
            "서울특별시 용산구",
            "서울특별시 성동구",
            "서울특별시 광진구",
            "서울특별시 동대문구",
            "서울특별시 중랑구",
            "서울특별시 성북구",
            "서울특별시 강북구",
            "서울특별시 도봉구",
            "서울특별시 노원구",
            "서울특별시 은평구",
            "서울특별시 서대문구",
            "서울특별시 마포구",
            "서울특별시 양천구",
            "서울특별시 강서구",
            "서울특별시 구로구",
            "서울특별시 금천구",
            "서울특별시 영등포구",
            "서울특별시 동작구",
            "서울특별시 관악구",
            "서울특별시 서초구",
            "서울특별시 강남구",
            "서울특별시 송파구",
            "서울특별시 강동구"
    };
    private static final String[] TYPES = {
            PropertyType.APARTMENT.name(),
            PropertyType.VILLA.name(),
            PropertyType.OFFICETEL.name()
    };
    private static final String[] APARTMENT_NAMES = {
            "래미안", "자이", "힐스테이트", "푸르지오", "롯데캐슬",
            "아이파크", "더샵", "센트럴파크", "e편한세상"
    };
    private static final String[] OFFICETEL_NAMES = {
            "시티뷰", "센트럴", "스카이", "비즈", "프라임",
            "파크", "타워", "리버뷰"
    };
    private static final String[] DESC_FEATURES = {
            "역앞", "초역세권", "버스정류장 인접",
            "엘리베이터 있음", "엘리베이터 없음",
            "주차 가능", "주차 불가",
            "신축", "준신축", "리모델링 완료",
            "남향", "동향", "서향",
            "채광 좋음", "조용한 주거지역",
            "편의시설 인접", "마트 근처", "공원 인접"
    };
    @Autowired
    DataSource dataSource;

    @Test
    void 매물_더미데이터_1000000개() throws SQLException, IOException {

        int total = 1000000;
        int batchSize = 10000;
        int commitSize = 10000;

        String sql = """
                INSERT INTO properties
                (name, address, floor, total_floor, room_count, type,
                built_year, description, price, migrate_date,
                supply_area, private_area, user_id, created_at, is_deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        long start = System.currentTimeMillis();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            connection.setAutoCommit(false);
            int count = 0;
            for (int i = 0; i < total; i++) {
                String address = ADDRESSES[R.nextInt(ADDRESSES.length)];
                int floor = R.nextInt(20) + 1;
                int totalFloor = R.nextInt(30) + 1;
                int roomCount = R.nextInt(4) + 1;
                String type = TYPES[R.nextInt(TYPES.length)];
                String name = createName(type);
                Year builtYear = Year.of(1980 + R.nextInt(45));
                String description = createDescription();
                BigDecimal price = BigDecimal.valueOf(100_000_000L + R.nextInt(900_000_000));
                LocalDate migrateDate = LocalDate.now().plusDays(R.nextInt(100));
                BigDecimal supplyArea = BigDecimal.valueOf(30 + R.nextInt(70));
                BigDecimal privateArea = supplyArea.subtract(BigDecimal.valueOf(5));
                long userId = R.nextLong(9) + 1;
                LocalDateTime createdAt = LocalDateTime.now();
                boolean isDeleted = false;

                ps.setString(1, name);
                ps.setString(2, address);
                ps.setInt(3, floor);
                ps.setInt(4, totalFloor);
                ps.setInt(5, roomCount);
                ps.setString(6, type);
                ps.setInt(7, builtYear.getValue()); // Year → int
                ps.setString(8, description);
                ps.setBigDecimal(9, price);
                ps.setDate(10, Date.valueOf(migrateDate));
                ps.setBigDecimal(11, supplyArea);
                ps.setBigDecimal(12, privateArea);
                ps.setLong(13, userId);
                ps.setTimestamp(14, Timestamp.valueOf(createdAt));
                ps.setBoolean(15, isDeleted);

                ps.addBatch();
                count++;

                if (count % batchSize == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }

                if (count % commitSize == 0) {
                    connection.commit();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("총 소요(ms) = " + (end - start));
        }
    }

    // 매물 명 생성 메서드
    private String createName(String type) {
        String name;
        if (PropertyType.APARTMENT.name().equals(type)) {
            String aptName = APARTMENT_NAMES[R.nextInt(APARTMENT_NAMES.length)];
            int dong = R.nextInt(20) + 101;
            int ho = R.nextInt(20) * 10 + 1;
            name = aptName + " 아파트 " + dong + "동 " + ho + "호";
        } else if (PropertyType.OFFICETEL.name().equals(type)) {
            String offName = OFFICETEL_NAMES[R.nextInt(OFFICETEL_NAMES.length)];
            int ho = R.nextInt(900) + 101;
            name = offName + " 오피스텔 " + ho + "호";
        } else {
            //VILLA
            int ho = R.nextInt(4) + 1;
            name = "빌라 " + ho + "호";
        }
        return name;
    }

    // description 생성 메서드
    private String createDescription() {
        int count = R.nextInt(3) + 2;
        List<String> picked = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            picked.add(DESC_FEATURES[R.nextInt(DESC_FEATURES.length)]);
        }

        return String.join(", ", picked);
    }
}
