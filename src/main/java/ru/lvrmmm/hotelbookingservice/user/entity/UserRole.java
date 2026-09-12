package ru.lvrmmm.hotelbookingservice.user.entity;

public enum UserRole{
    USER, //простой пользователь, бронирует номера, может смотреть список номеров
    MANAGER, //может поставить номер на "стоп"
    ADMIN, // может все,что доступно USER и MANAGER + может заблокировать пользователя, назначить менеджера и тп
}
