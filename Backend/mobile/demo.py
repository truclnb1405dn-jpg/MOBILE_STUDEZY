from django.utils import timezone
from datetime import date, datetime, timedelta
from studezy.models import User, Semester, ClassSchedule, Deadline

# 1. TẠO TÀI KHOẢN NGƯỜI DÙNG
# Dùng get_or_create để lỡ chạy lại script cũng không bị lỗi trùng lặp
user, created = User.objects.get_or_create(
    username='truc123',
    defaults={
        'full_name': 'Lê Nguyễn Bảo Trúc',
        'phone_number': '0777446184',
        'email': 'truc@studezy.com'
    }
)
if created:
    user.set_password('truc123@')  # Mã hóa mật khẩu an toàn
    user.save()
    print("✅ Đã tạo user Bảo Trúc")

# Xóa dữ liệu cũ (nếu có) để nạp lại từ đầu cho sạch
Semester.objects.filter(user=user).delete()

# 2. TẠO HỌC KỲ
semester = Semester.objects.create(
    user=user,
    name='Học kỳ II năm 2025-2026',
    start_date=date(2025, 4, 10),
    end_date=date(2025, 6, 10)
)
print("✅ Đã tạo Học kỳ")

# 3. TẠO LỊCH HỌC
schedules = [
    {'sub': 'Kiểm thử phần mềm', 'day': 2, 'start': '13:30:00', 'end': '16:00:00', 'room': 'Phòng A305',
     'color': '#FF5722'},
    {'sub': 'Lập trình web', 'day': 4, 'start': '09:45:00', 'end': '12:25:00', 'room': 'Phòng A404',
     'color': '#4CAF50'},
    {'sub': 'Kiểm thử phần mềm', 'day': 5, 'start': '13:30:00', 'end': '16:00:00', 'room': 'Phòng A305',
     'color': '#FF5722'},
    {'sub': 'Quản trị dự án', 'day': 6, 'start': '07:00:00', 'end': '09:40:00', 'room': 'Phòng A307',
     'color': '#9C27B0'},
    {'sub': 'Thực hành phân tích thiết kế', 'day': 6, 'start': '16:15:00', 'end': '19:00:00', 'room': 'Phòng A214',
     'color': '#03A9F4'},
    {'sub': 'Lập trình ứng dụng di động', 'day': 7, 'start': '07:00:00', 'end': '09:40:00', 'room': 'Phòng A307',
     'color': '#1E3A8A'},
    {'sub': 'Quản trị dự án', 'day': 7, 'start': '13:30:00', 'end': '16:15:00', 'room': 'Phòng A307',
     'color': '#9C27B0'},
]

for s in schedules:
    ClassSchedule.objects.create(
        user=user, semester=semester, subject_name=s['sub'], day_of_week=s['day'],
        start_time=s['start'], end_time=s['end'], room=s['room'], color_hex=s['color']
    )
print("✅ Đã nạp xong Lịch học")

# 4. TẠO DEADLINE TỪ 12/04 ĐẾN 24/04 (Năm 2026)
deadlines_data = [
    # Đã qua & Đã hoàn thành
    (12, 'Tóm tắt yêu cầu Web', 'T', True),
    (13, 'Cài đặt môi trường Kiểm thử', 'B', True),
    (14, 'Nộp danh sách nhóm QLDA', 'B', True),
    (15, 'Vẽ Use Case diagram', 'T', True),
    (16, 'Clone code ứng dụng di động', 'B', True),
    (17, 'Viết test plan sơ bộ', 'B', True),

    # Hôm nay (18/04) - Chưa hoàn thành, RẤT GẤP (Duy nhất 1 cái)
    (18, 'Nộp bài tập UI/UX Mobile', 'G', False),

    # Tương lai - Chưa hoàn thành, Thong thả / Bình thường
    (19, 'Báo cáo tiến độ đồ án', 'B', False),
    (20, 'Làm bài Quiz Kiểm thử', 'B', False),
    (21, 'Đọc tài liệu ReactJS', 'T', False),
    (22, 'Cập nhật Trello nhóm', 'T', False),
    (23, 'Vẽ biểu đồ Activity', 'B', False),
    (24, 'Nộp source code giữa kỳ', 'B', False),
]

for day, title, priority, is_completed in deadlines_data:
    # Cấu hình ngày giờ có đính kèm timezone để không bị lỗi ValueError
    dt = timezone.make_aware(datetime(2026, 4, day, 23, 59, 0))
    Deadline.objects.create(
        user=user, title=title, due_date=dt, priority=priority, is_completed=is_completed
    )

print("✅ Đã nạp xong 13 Deadline!")
print("🎉 HOÀN TẤT NẠP DỮ LIỆU. Bạn có thể gõ exit() để thoát.")