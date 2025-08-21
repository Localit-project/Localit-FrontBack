package com.inhatc.localit.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {TouristSpot.class}, version = 2, exportSchema = false) // ✨ 1. 버전 번호를 1 올립니다 (예: 1 -> 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TouristSpotDao touristSpotDao();

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // 데이터베이스 인스턴스를 가져오는 static 메서드 (싱글톤 구현)
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "localit_database")
                            // ✨ 2. 이 위치에 추가합니다.
                            // 데이터베이스 스키마(구조)가 변경되었을 때 기존 데이터를 모두 삭제하고 새로 만듭니다.
                            // 개발 중에만 사용해야 하며, 출시된 앱에서는 사용자 데이터가 사라지므로 절대 사용하면 안 됩니다.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}