package com.inhatc.localit.db; // 본인의 패키지 이름으로 변경하세요

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room 데이터베이스를 설정하고 관리하는 메인 클래스입니다.
 */
// ▼▼▼▼▼ 1. version 숫자를 1에서 2로 올립니다 ▼▼▼▼▼
@Database(entities = {TouristSpot.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TouristSpotDao touristSpotDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "tourist_spot_database")
                            // ▼▼▼▼▼ 2. 이 한 줄을 추가합니다 ▼▼▼▼▼
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
