from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.authtoken.models import Token
from rest_framework.permissions import IsAuthenticated
from rest_framework.authentication import TokenAuthentication
from django.utils import timezone
from django.contrib.auth import authenticate
from .models import User, ClassSchedule, Deadline


class LoginAPIView(APIView):
    def post(self, request):
        # Lấy username và password từ Android gửi lên
        username = request.data.get('username')
        password = request.data.get('password')

        # Nhờ Django kiểm tra xem có khớp trong SQL Server không
        user = authenticate(username=username, password=password)

        if user is not None:
            # Nếu đúng, tạo hoặc lấy Token của người dùng này
            token, created = Token.objects.get_or_create(user=user)

            # Trả về kết quả thành công cho Android
            return Response({
                'status': 'success',
                'message': 'Đăng nhập thành công',
                'token': token.key,
                'full_name': user.full_name,  # Gửi kèm tên để Android hiển thị ở Trang chủ
            }, status=status.HTTP_200_OK)
        else:
            # Trả về lỗi nếu sai tài khoản/mật khẩu
            return Response({
                'status': 'error',
                'message': 'Tên đăng nhập hoặc mật khẩu không chính xác'
            }, status=status.HTTP_401_UNAUTHORIZED)


class RegisterAPIView(APIView):
    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')
        full_name = request.data.get('full_name')
        email = request.data.get('email', '')


        if not username or not password or not full_name:
            return Response({'status': 'error', 'message': 'Vui lòng điền đầy đủ thông tin'},
                            status=status.HTTP_400_BAD_REQUEST)

        if User.objects.filter(username=username).exists():
            return Response({'status': 'error', 'message': 'Tên đăng nhập đã có người sử dụng'},
                            status=status.HTTP_400_BAD_REQUEST)

        # Truyền thêm agreed_to_terms vào lúc tạo user
        user = User.objects.create_user(
            username=username,
            password=password,
            full_name=full_name,
            email=email,
        )

        return Response({'status': 'success', 'message': 'Đăng ký tài khoản thành công!'},
                        status=status.HTTP_201_CREATED)


class HomeSummaryAPIView(APIView):

    authentication_classes = [TokenAuthentication]
    # Bắt buộc Android phải gửi kèm Token khi gọi API này
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user

        # Lấy ngày và giờ hiện tại
        now = timezone.localtime(timezone.now())

        # 1. TÍNH SỐ TIẾT HỌC HÔM NAY
        # Hàm weekday() của Python trả về: 0 (Thứ 2) -> 6 (Chủ nhật)
        # DAY_CHOICES của ta là: 2 (Thứ 2) -> 8 (Chủ nhật) -> Cộng thêm 2 để khớp logic
        django_weekday = now.weekday() + 2
        classes_today = ClassSchedule.objects.filter(user=user, day_of_week=django_weekday).count()

        # 2. TÍNH SỐ DEADLINE HÔM NAY
        # Chỉ đếm những deadline chưa làm (is_completed=False) và hạn nộp nằm trong ngày hôm nay
        deadlines_today = Deadline.objects.filter(user=user, due_date__date=now.date(), is_completed=False).count()

        return Response({
            'classes_today': classes_today,
            'deadlines_today': deadlines_today
        })


class ClassesTodayAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.localtime(timezone.now())
        django_weekday = now.weekday() + 2

        # Truy vấn các môn học của hôm nay, sắp xếp theo giờ bắt đầu
        classes = ClassSchedule.objects.filter(user=user, day_of_week=django_weekday).order_by('start_time')

        data = []
        for c in classes:
            # Format giờ thẳng từ Backend để Android đỡ phải xử lý (VD: 07:00 -> 07h00)
            start_str = c.start_time.strftime('%Hh%M').replace('h00', 'h')
            end_str = c.end_time.strftime('%Hh%M').replace('h00', 'h')

            data.append({
                'subject_name': c.subject_name,  # Tên trường này phụ thuộc vào models.py của bạn
                'room': c.room,
                'time_string': f"Giờ  •  {start_str} - {end_str}"
            })

        return Response(data)

class TopDeadlinesAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.localtime(timezone.now())

        # Lấy 3 deadline chưa làm và có hạn nộp trong tương lai gần nhất
        deadlines = Deadline.objects.filter(
            user=user,
            is_completed=False,
            due_date__gte=now
        ).order_by('due_date')[:3]

        data = []
        for d in deadlines:
            diff = d.due_date - now
            days = diff.days
            hours = diff.seconds // 3600

            # Logic kiểm tra gấp gáp: Nếu hạn <= 1 ngày (dưới 48 tiếng)
            is_urgent = False
            if days == 0:
                is_urgent = True
                remaining_text = f"Còn {hours} giờ" if hours > 0 else "Dưới 1 giờ"
            else:
                remaining_text = f"Còn {days} ngày"
                if days == 1:
                    is_urgent = True # 1 ngày vẫn tính là đỏ theo giao diện mẫu

            data.append({
                'title': d.title,
                'remaining_text': remaining_text,
                'is_urgent': is_urgent
            })

        return Response(data)