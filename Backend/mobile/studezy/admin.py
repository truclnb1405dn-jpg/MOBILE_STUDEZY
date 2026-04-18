from django.contrib import admin
from .models import User, Semester, ClassSchedule, Deadline

# Khai báo hiển thị bảng User
@admin.register(User)
class UserAdmin(admin.ModelAdmin):
    list_display = ('username', 'full_name', 'email', 'phone_number', 'is_active')
    search_fields = ('username', 'full_name')

# Khai báo hiển thị bảng Học kỳ
@admin.register(Semester)
class SemesterAdmin(admin.ModelAdmin):
    list_display = ('name', 'start_date', 'end_date', 'user')

# Khai báo hiển thị bảng Lịch học
@admin.register(ClassSchedule)
class ClassScheduleAdmin(admin.ModelAdmin):
    list_display = ('subject_name', 'get_day_of_week_display', 'start_time', 'end_time', 'room', 'user')
    list_filter = ('day_of_week', 'semester')
    search_fields = ('subject_name', 'room')

# Khai báo hiển thị bảng Deadline
@admin.register(Deadline)
class DeadlineAdmin(admin.ModelAdmin):
    list_display = ('title', 'due_date', 'priority', 'is_completed', 'user')
    list_filter = ('is_completed', 'priority')
    search_fields = ('title',)