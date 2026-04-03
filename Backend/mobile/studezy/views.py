from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.authtoken.models import Token
from django.contrib.auth import authenticate
from .models import User


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

