package com.inhatc.localit.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room 데이터베이스를 설정하고 관리하는 메인 클래스입니다.
 * - DB 버전이 변경되면 반드시 version 값을 올리고 마이그레이션을 처리해야 합니다.
 */
@Database(entities = {TouristSpot.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    // DAO 정의
    public abstract TouristSpotDao touristSpotDao();

    // 싱글톤 인스턴스
    private static volatile AppDatabase INSTANCE;

    // 백그라운드 DB 작업용 스레드풀
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // 데이터베이스 인스턴스 가져오기
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tourist_spot_database")


                            .build();
                }
            }
        }
        return INSTANCE;
    }
}