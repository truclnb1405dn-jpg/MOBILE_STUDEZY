from django.db import models
from django.contrib.auth.models import AbstractUser, Group, Permission


# 1. BẢNG NGƯỜI DÙNG (Tùy biến từ User mặc định của Django)
class User(AbstractUser):
    # --- THÊM TRƯỜNG NÀY ĐỂ KHỚP VỚI MÀN HÌNH ANDROID ---
    full_name = models.CharField(max_length=150, verbose_name="Họ và tên")

    # Các trường tùy chọn khác của bạn
    phone_number = models.CharField(max_length=15, blank=True, null=True, verbose_name="Số điện thoại")

    # --- BẮT BUỘC THÊM ĐỂ SỬA LỖI E304 ---
    groups = models.ManyToManyField(
        Group,
        verbose_name='groups',
        blank=True,
        help_text='The groups this user belongs to.',
        related_name="studezy_users_groups",
        related_query_name="user",
    )
    user_permissions = models.ManyToManyField(
        Permission,
        verbose_name='user permissions',
        blank=True,
        help_text='Specific permissions for this user.',
        related_name="studezy_users_permissions",
        related_query_name="user",
    )

    # --------------------------------------

    def __str__(self):
        return self.username


# 2. BẢNG HỌC KỲ (Dành cho việc cài đặt lịch học theo học kỳ)
class Semester(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='semesters')
    name = models.CharField(max_length=100, verbose_name="Tên học kỳ")  # VD: Học kỳ 1 - Năm học 2025-2026
    start_date = models.DateField(verbose_name="Ngày bắt đầu")
    end_date = models.DateField(verbose_name="Ngày kết thúc")

    def __str__(self):
        return f"{self.name} - {self.user.username}"


# 3. BẢNG LỊCH HỌC
class ClassSchedule(models.Model):
    DAY_CHOICES = [
        (2, 'Thứ 2'),
        (3, 'Thứ 3'),
        (4, 'Thứ 4'),
        (5, 'Thứ 5'),
        (6, 'Thứ 6'),
        (7, 'Thứ 7'),
        (8, 'Chủ nhật'),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='class_schedules')

    # --- BẮT BUỘC ĐỔI THÀNH DO_NOTHING ĐỂ TRÁNH LỖI VÒNG LẶP CỦA SQL SERVER ---
    semester = models.ForeignKey(Semester, on_delete=models.DO_NOTHING, related_name='classes')

    subject_name = models.CharField(max_length=200, verbose_name="Tên môn học")
    day_of_week = models.IntegerField(choices=DAY_CHOICES, verbose_name="Thứ trong tuần")

    start_time = models.TimeField(verbose_name="Giờ vào lớp")
    end_time = models.TimeField(verbose_name="Giờ kết thúc")
    color_hex = models.CharField(max_length=7, default="#2196F3", verbose_name="Mã màu hiển thị")

    room = models.CharField(max_length=50, verbose_name="Phòng học")
    note = models.TextField(blank=True, null=True, verbose_name="Ghi chú")

    def __str__(self):
        return f"{self.subject_name} - {self.get_day_of_week_display()}"


# 4. BẢNG NHIỆM VỤ / DEADLINE
class Deadline(models.Model):
    PRIORITY_CHOICES = [
        ('G', 'Gấp'),
        ('B', 'Bình thường'),
        ('T', 'Thong thả'),
    ]

    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='deadlines')
    title = models.CharField(max_length=200, verbose_name="Tên deadline")
    description = models.TextField(blank=True, null=True, verbose_name="Mô tả chi tiết")
    due_date = models.DateTimeField(verbose_name="Hạn deadline")

    reminder_time = models.DateTimeField(blank=True, null=True, verbose_name="Thời gian nhắc trước")
    priority = models.CharField(max_length=1, choices=PRIORITY_CHOICES, default='B', verbose_name="Mức độ ưu tiên")

    # Cờ trạng thái độc lập để kiểm soát việc đã xong hay chưa
    is_completed = models.BooleanField(default=False, verbose_name="Trạng thái hoàn thành")

    # Lưu vết thời gian tạo và cập nhật
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.title