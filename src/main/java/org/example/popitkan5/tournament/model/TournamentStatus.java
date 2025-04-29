package org.example.popitkan5.tournament.model;

public enum TournamentStatus {
    DRAFT,          // Черновик, турнир создан но не запущен
    IN_PROGRESS,    // В процессе, турнир запущен
    FINISHED,       // Завершен, есть победитель
    CLOSED          // Закрыт, архивирован
}
