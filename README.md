# LargeFileFinder

## Опис проєкту
Ця програма дозволяє проходити по файлах у вказаній користувачем директорії та знаходити всі файли, розмір яких перевищує заданий поріг (у байтах). Програма реалізована з використанням двох підходів до багатопотокової обробки: **Fork/Join Framework** та **ThreadPool Executor Service**.

---

## Чому обрано Fork/Join Framework і ThreadPool?

### Fork/Join Framework
**Причина вибору**:
- Fork/Join Framework ідеально підходить для рекурсивних задач, таких як обхід вкладених папок.
- Цей підхід використовує техніку **Work-Stealing**, що дозволяє потокам, які завершили роботу, виконувати задачі інших потоків, підвищуючи ефективність.

**Переваги**:
- Добре масштабується для великої кількості вкладених директорій.
- Проста реалізація через використання класу `RecursiveTask`.

**Недоліки**:
- Може споживати більше пам’яті через глибоку рекурсію.

---

### ThreadPool
**Причина вибору**:
- ThreadPool добре підходить для задач, де відомо, скільки робіт потрібно виконати (наприклад, для файлів у директорії).
- Використовує техніку **Work-Dealing**, де задачі розподіляються між потоками заздалегідь.

**Переваги**:
- Простий контроль кількості потоків через `ExecutorService`.
- Добре працює у випадках з обмеженою кількістю підзадач.

**Недоліки**:
- Менш ефективний, якщо вкладених директорій дуже багато.

---

## Який підхід кращий?
- **Fork/Join Framework** підходить для великих вкладених директорій з численними підпапками, оскільки дозволяє ефективно розділяти рекурсивні задачі.
- **ThreadPool** краще підходить для простих структур директорій, де завдань не дуже багато і вони не потребують складної рекурсії.

**Висновок**:
Обидва підходи реалізовані для демонстрації їх роботи та можливості порівняння. Fork/Join Framework обрано через його ефективність для рекурсивних задач, тоді як ThreadPool додано для балансу між простотою реалізації та продуктивністю.

---

## Як використовувати програму

1. **Клонуйте репозиторій:**
   ```bash
   git clone https://github.com/your-username/LargeFileFinder.git
2. **Скомпілюйте програму:**

   ```bash
   javac LargeFileFinder.java
3. **Запустіть програму:**

   ```bash
   java LargeFileFinder
4. **Введіть шлях до директорії та пороговий розмір файлів у байтах.**

**Приклад роботи**

```plaintext
Enter the directory path: C:\example\directory
Enter the file size threshold (in bytes): 1048576

Large Files Found (Fork/Join): 12
Execution Time (Fork/Join): 15 ms

Large Files Found (ThreadPool): 12
Execution Time (ThreadPool): 13 ms
