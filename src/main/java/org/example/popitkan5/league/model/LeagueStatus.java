package org.example.popitkan5.league.model;

public enum LeagueStatus {
    DRAFT,          // Черновик, лига создана но не запущена
    REGISTRATION,   // В процессе регистрации
    ACTIVE,         // Активна, лига запущена
    FINISHED,       // Завершена
    CANCELED        // Отменена
}
