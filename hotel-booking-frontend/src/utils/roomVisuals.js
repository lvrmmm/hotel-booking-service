// Переводы enum-значений backend'а в читаемые русские подписи

export const OCCUPANCY_LABELS = {
  SINGLE: "Одноместный",
  DOUBLE: "Двухместный",
  TWIN: "Твин",
  TRIPLE: "Трёхместный",
  QUADRUPLE: "Четырёхместный",
  FAMILY: "Семейный",
};

export const COMFORT_LABELS = {
  STANDARD: "Стандарт",
  SUPERIOR: "Улучшенный",
  DELUXE: "Делюкс",
  JUNIOR_SUITE: "Джуниор-сьют",
  SUITE: "Люкс",
  STUDIO: "Студия",
  APARTMENT: "Апартаменты",
  ACCESSIBLE: "Для маломобильных гостей",
};

export const STATUS_LABELS = {
  PENDING: "Ожидает подтверждения",
  CONFIRMED: "Подтверждено",
  CANCELLED: "Отменено",
  COMPLETED: "Завершено",
};

export const ROLE_LABELS = {
  USER: "Гость",
  MANAGER: "Менеджер",
  ADMIN: "Администратор",
};

// Каждому уровню комфорта — свой градиент в тонах основной палитры,
// чтобы карточки различались без использования фотографий.
const COMFORT_GRADIENTS = {
  STANDARD: "linear-gradient(135deg, #6b6357, #3d382f)",
  SUPERIOR: "linear-gradient(135deg, #4d6a5a, #223226)",
  DELUXE: "linear-gradient(135deg, #a85c32, #6b3a1f)",
  JUNIOR_SUITE: "linear-gradient(135deg, #7c9081, #3c483f)",
  SUITE: "linear-gradient(135deg, #814526, #401f10)",
  STUDIO: "linear-gradient(135deg, #5c6e63, #2b352e)",
  APARTMENT: "linear-gradient(135deg, #6e7a4f, #343a24)",
  ACCESSIBLE: "linear-gradient(135deg, #5a6b73, #29343a)",
};

export function roomGradient(comfortLevel) {
  return COMFORT_GRADIENTS[comfortLevel] || COMFORT_GRADIENTS.STANDARD;
}

export function formatDate(isoDate) {
  return new Date(isoDate).toLocaleDateString("ru-RU", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
}

export function formatPrice(value) {
  return new Intl.NumberFormat("ru-RU", { minimumFractionDigits: 0 }).format(value);
}
