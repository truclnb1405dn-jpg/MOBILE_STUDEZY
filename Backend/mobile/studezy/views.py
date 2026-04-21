from django.shortcuts import get_object_or_404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.authtoken.models import Token
from rest_framework.permissions import IsAuthenticated
from rest_framework.authentication import TokenAuthentication
from django.utils import timezone
from django.contrib.auth import authenticate
from .models import User, ClassSchedule, Deadline, Semester
from datetime import datetime, timedelta
from django.contrib.auth.password_validation import validate_password
from django.core.exceptions import ValidationError


class LoginAPIView(APIView):
    def post(self, request):
        username = request.data.get('username')
        password = request.data.get('password')

        user = authenticate(username=username, password=password)

        if user is not None:
            token, created = Token.objects.get_or_create(user=user)
            return Response({
                'status': 'success',
                'message': 'Đăng nhập thành công',
                'token': token.key,
                'full_name': user.full_name,
            }, status=status.HTTP_200_OK)
        else:
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
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        now = timezone.localtime(timezone.now())

        django_weekday = now.weekday() + 2
        classes_today = ClassSchedule.objects.filter(user=user, day_of_week=django_weekday).count()

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

        classes = ClassSchedule.objects.filter(user=user, day_of_week=django_weekday).order_by('start_time')

        data = []
        for c in classes:
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

            is_urgent = False
            if days == 0:
                is_urgent = True
                remaining_text = f"Còn {hours} giờ" if hours > 0 else "Dưới 1 giờ"
            else:
                remaining_text = f"Còn {days} ngày"
                if days == 1:
                    is_urgent = True

            data.append({
                'id': d.id,
                'title': d.title,
                'remaining_text': remaining_text,
                'is_urgent': is_urgent,
                'is_completed': d.is_completed
            })

        return Response(data)


class ClassesByDateAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        target_date_str = request.query_params.get('date')

        if not target_date_str:
            return Response({"error": "Thiếu tham số ngày (date)"}, status=status.HTTP_400_BAD_REQUEST)

        try:
            # Chuyển đổi chuỗi "YYYY-MM-DD" từ Android thành object date của Python
            target_date = datetime.strptime(target_date_str, '%Y-%m-%d').date()
        except ValueError:
            return Response({"error": "Sai định dạng ngày."}, status=status.HTTP_400_BAD_REQUEST)

        # Tính toán thứ trong tuần cho ngày được chọn (weekday() trả về 0: Thứ 2 -> 6: Chủ nhật)
        # Cộng thêm 2 để khớp với logic DAY_CHOICES của bạn (2: Thứ 2 -> 8: Chủ nhật)
        django_weekday = target_date.weekday() + 2

        # Lọc các môn học của thứ đó
        classes = ClassSchedule.objects.filter(user=user, day_of_week=django_weekday).order_by('start_time')

        data = []
        for c in classes:
            start_str = c.start_time.strftime('%Hh%M').replace('h00', 'h')
            end_str = c.end_time.strftime('%Hh%M').replace('h00', 'h')

            data.append({
                'subject_name': c.subject_name,
                'room': c.room,
                'time_string': f"Giờ  •  {start_str} - {end_str}"
            })

        return Response(data)


class DeadlinesByDateAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        target_date_str = request.query_params.get('date')
        now = timezone.localtime(timezone.now())
        today = now.date()

        if not target_date_str:
            return Response({"error": "Thiếu tham số ngày (date)"}, status=status.HTTP_400_BAD_REQUEST)

        try:
            target_date = datetime.strptime(target_date_str, '%Y-%m-%d').date()
        except ValueError:
            return Response({"error": "Sai định dạng ngày."}, status=status.HTTP_400_BAD_REQUEST)

        # 1. LẤY TẤT CẢ DEADLINE CỦA NGÀY ĐƯỢC CHỌN
        target_deadlines = list(Deadline.objects.filter(
            user=user,
            due_date__date=target_date
        ))

        # 2. KIỂM TRA LOGIC NẾU NGÀY CHỌN >= HÔM NAY
        if target_date >= today:
            # Đếm số deadline chưa hoàn thành trong ngày này
            uncompleted_count = sum(1 for d in target_deadlines if not d.is_completed)

            # Nếu chưa hoàn thành < 3, lấy bù thêm từ các ngày TƯƠNG LAI
            if uncompleted_count < 3:
                needed = 3 - uncompleted_count

                future_deadlines = list(Deadline.objects.filter(
                    user=user,
                    is_completed=False,
                    due_date__date__gt=target_date  # Chỉ lấy của các ngày sau ngày được chọn
                ).order_by('due_date')[:needed])

                target_deadlines.extend(future_deadlines)

        # 3. SẮP XẾP LẠI (QUAN TRỌNG)
        # Đẩy các task đã hoàn thành (is_completed=True) xuống dưới cùng
        # Các task chưa hoàn thành sẽ nổi lên trên và xếp theo thời gian nộp
        target_deadlines.sort(key=lambda d: (d.is_completed, d.due_date))

        # 4. CHUẨN BỊ DỮ LIỆU GỬI VỀ ANDROID
        data = []
        for d in target_deadlines:
            diff = d.due_date - now
            days = diff.days
            hours = diff.seconds // 3600
            is_urgent = False

            if days < 0:
                remaining_text = "Đã quá hạn"
                is_urgent = True
            elif days == 0:
                is_urgent = True
                remaining_text = f"Còn {hours} giờ" if hours > 0 else "Dưới 1 giờ"
            else:
                remaining_text = f"Còn {days} ngày"
                if days == 1:
                    is_urgent = True

            data.append({
                'id': d.id,
                'title': d.title,
                'remaining_text': remaining_text,
                'is_urgent': is_urgent,
                'is_completed': d.is_completed
            })

        return Response(data)

class ToggleDeadlineAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request, pk):
        # Tìm deadline theo ID (pk) và đảm bảo nó thuộc về user đang đăng nhập
        deadline = get_object_or_404(Deadline, pk=pk, user=request.user)

        # Lấy trạng thái mới từ Android gửi lên
        is_completed = request.data.get('is_completed')

        if is_completed is not None:
            deadline.is_completed = is_completed
            deadline.save()  # Lưu vào Cơ sở dữ liệu
            return Response({'status': 'success', 'message': 'Cập nhật thành công!'})

        return Response({'status': 'error', 'message': 'Thiếu dữ liệu is_completed'},
                            status=status.HTTP_400_BAD_REQUEST)

class TasksAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        search_keyword = request.GET.get('search', '').strip()
        time_filter = request.GET.get('filter', 'all')
        now = timezone.localtime(timezone.now())
        queryset = Deadline.objects.filter(user=user)

        if search_keyword:
            queryset = queryset.filter(title__icontains=search_keyword)

        if time_filter == 'today':
            queryset = queryset.filter(due_date__date=now.date(), is_completed=False)
        elif time_filter == 'week':
            monday = now.date() - timedelta(days=now.weekday())
            sunday = monday + timedelta(days=6)
            queryset = queryset.filter(due_date__date__gte=monday, due_date__date__lte=sunday, is_completed=False)
        elif time_filter == 'completed':
            queryset = queryset.filter(is_completed=True)
        else:
            queryset = queryset.order_by('is_completed', 'due_date')

        data = []
        for d in queryset:
            status_code = 1 if d.is_completed else 0
            time_left_str = "Đã hoàn thành"
            if not d.is_completed:
                if d.due_date < now:
                    time_left_str = "Quá hạn!"
                else:
                    diff = d.due_date - now
                    days = diff.days
                    hours = diff.seconds // 3600
                    if days > 0:
                        time_left_str = f"Còn {days} ngày"
                    elif hours > 0:
                        time_left_str = f"Còn {hours} giờ"
                    else:
                        time_left_str = "Dưới 1 giờ"

            data.append({
                'id': d.id,
                'title': d.title,
                'description': getattr(d, 'description', 'Chi tiết nhiệm vụ...'),
                'deadline_date': d.due_date.strftime("%d/%m/%Y"),
                'time_left': time_left_str,
                'status': status_code
            })
        return Response(data)

    def post(self, request):
        user = request.user
        title = request.data.get('title')
        description = request.data.get('description', '')
        due_date_str = request.data.get('due_date')

        if not title or not due_date_str:
            return Response({'error': 'Thiếu thông tin'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            from datetime import datetime
            from django.utils.timezone import make_aware, get_default_timezone

            if len(due_date_str) > 10:
                naive_dt = datetime.strptime(due_date_str, "%d/%m/%Y %H:%M")
            else:
                naive_dt = datetime.strptime(due_date_str, "%d/%m/%Y")

            aware_dt = make_aware(naive_dt, get_default_timezone())

            new_task = Deadline.objects.create(
                user=user,
                title=title,
                description=description,
                due_date=aware_dt,
                is_completed=False
            )

            now = timezone.localtime(timezone.now())
            diff = new_task.due_date - now
            days = diff.days
            hours = diff.seconds // 3600

            if days > 0:
                time_left_str = f"Còn {days} ngày"
            elif hours > 0:
                time_left_str = f"Còn {hours} giờ"
            else:
                time_left_str = "Dưới 1 giờ"

            response_data = {
                'id': new_task.id,
                'title': new_task.title,
                'description': getattr(new_task, 'description', 'Chi tiết nhiệm vụ...'),
                'deadline_date': new_task.due_date.strftime("%d/%m/%Y"),
                'time_left': time_left_str,
                'status': 0
            }
            return Response(response_data, status=status.HTTP_201_CREATED)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class UpdateTaskStatusAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request, pk):
        try:
            task = Deadline.objects.get(pk=pk, user=request.user)
            new_status = request.data.get('status')
            task.is_completed = (new_status == 1)
            task.save()
            return Response({'status': 'success', 'is_completed': task.is_completed}, status=status.HTTP_200_OK)
        except Deadline.DoesNotExist:
            return Response({'status': 'error', 'message': 'Không tìm thấy nhiệm vụ'}, status=status.HTTP_404_NOT_FOUND)


class EditTaskAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request, pk):
        try:
            task = Deadline.objects.get(pk=pk, user=request.user)
            title = request.data.get('title')
            description = request.data.get('description', '')
            due_date_str = request.data.get('due_date')

            if not title or not due_date_str:
                return Response({'error': 'Thiếu thông tin'}, status=status.HTTP_400_BAD_REQUEST)

            from datetime import datetime
            from django.utils.timezone import make_aware, get_default_timezone

            if len(due_date_str) > 10:
                naive_dt = datetime.strptime(due_date_str, "%d/%m/%Y %H:%M")
            else:
                naive_dt = datetime.strptime(due_date_str, "%d/%m/%Y")

            aware_dt = make_aware(naive_dt, get_default_timezone())

            task.title = title
            task.description = description
            task.due_date = aware_dt
            task.save()

            now = timezone.localtime(timezone.now())
            time_left_str = "Đã hoàn thành"
            if not task.is_completed:
                if task.due_date < now:
                    time_left_str = "Quá hạn!"
                else:
                    diff = task.due_date - now
                    days = diff.days
                    hours = diff.seconds // 3600
                    if days > 0:
                        time_left_str = f"Còn {days} ngày"
                    elif hours > 0:
                        time_left_str = f"Còn {hours} giờ"
                    else:
                        time_left_str = "Dưới 1 giờ"

            response_data = {
                'id': task.id,
                'title': task.title,
                'description': getattr(task, 'description', ''),
                'deadline_date': task.due_date.strftime("%d/%m/%Y"),
                'time_left': time_left_str,
                'status': 1 if task.is_completed else 0
            }
            return Response(response_data, status=status.HTTP_200_OK)

        except Deadline.DoesNotExist:
            return Response({'error': 'Không tìm thấy nhiệm vụ'}, status=status.HTTP_404_NOT_FOUND)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)


class DeleteTaskAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def delete(self, request, pk):
        try:
            task = Deadline.objects.get(pk=pk, user=request.user)
            task.delete()
            return Response({'status': 'success', 'message': 'Đã xoá nhiệm vụ'}, status=status.HTTP_200_OK)
        except Deadline.DoesNotExist:
            return Response({'error': 'Không tìm thấy nhiệm vụ'}, status=status.HTTP_404_NOT_FOUND)

class AddClassScheduleAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        # Tự động lấy hoặc tạo semester mặc định (vì form chưa có chọn semester)
        semester, _ = Semester.objects.get_or_create(
            user=user,
            name="Học kỳ 1 - 2025-2026",
            defaults={
                'start_date': timezone.now().date(),
                'end_date': timezone.now().date() + timedelta(days=180)
            }
        )

        try:
            # Xử lý thời gian và ngày học
            start_time_str = request.data.get('start_time')  # "07:30"
            start_time = datetime.strptime(start_time_str, '%H:%M').time()
            day_of_week = int(request.data['day_of_week'])

            # === KIỂM TRA TRÙNG LỊCH ===
            # Tìm xem user này, vào Thứ này, Giờ này đã có môn nào chưa
            is_conflict = ClassSchedule.objects.filter(
                user=user,
                day_of_week=day_of_week,
                start_time=start_time
            ).exists()

            if is_conflict:
                return Response({
                    'status': 'error',
                    'message': 'Khung giờ này đã bị trùng với lịch học khác!'
                }, status=status.HTTP_400_BAD_REQUEST)
            # ============================

            end_time = (datetime.combine(datetime.min, start_time) + timedelta(hours=2, minutes=30)).time()

            # Nếu không trùng, tiến hành tạo mới
            ClassSchedule.objects.create(
                user=user,
                semester=semester,
                subject_name=request.data['subject_name'],
                day_of_week=day_of_week,
                start_time=start_time,
                end_time=end_time,
                room=request.data['room'],
                note=request.data.get('note', ''),
                color_hex="#4385F4"   # màu chính của app
            )

            return Response({
                'status': 'success',
                'message': 'Thêm lịch học thành công!'
            }, status=status.HTTP_201_CREATED)

        except Exception as e:
            return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class AllClassSchedulesAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        schedules = ClassSchedule.objects.filter(user=user).order_by('day_of_week', 'start_time')

        data = []
        for s in schedules:
            start_str = s.start_time.strftime('%Hh%M').replace('h00', 'h')
            end_str = s.end_time.strftime('%Hh%M').replace('h00', 'h')
            data.append({
                'id': s.id,
                'subject_name': s.subject_name,
                'day_of_week': str(s.day_of_week),
                'time_string': f"{start_str} - {end_str}",
                'room': s.room,
                'color_hex': s.color_hex,
                'note': s.note or ''
            })
        return Response(data)


class ClassScheduleDetailAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    # Hàm XÓA lịch học
    def delete(self, request, pk):
        try:
            # Tìm môn học dựa vào ID (pk) và phải thuộc về user đang đăng nhập
            schedule = ClassSchedule.objects.get(pk=pk, user=request.user)
            schedule.delete()
            return Response({'status': 'success', 'message': 'Xóa môn học thành công!'})
        except ClassSchedule.DoesNotExist:
            return Response({'status': 'error', 'message': 'Không tìm thấy môn học'}, status=status.HTTP_404_NOT_FOUND)

    # Hàm SỬA lịch học
    def put(self, request, pk):
        try:
            schedule = ClassSchedule.objects.get(pk=pk, user=request.user)

            # Cập nhật các trường
            schedule.subject_name = request.data.get('subject_name', schedule.subject_name)
            schedule.day_of_week = int(request.data.get('day_of_week', schedule.day_of_week))
            schedule.room = request.data.get('room', schedule.room)
            schedule.note = request.data.get('note', schedule.note)

            # Xử lý giờ (nếu có cập nhật)
            start_time_str = request.data.get('start_time')
            if start_time_str:
                schedule.start_time = datetime.strptime(start_time_str, '%H:%M').time()
                schedule.end_time = (datetime.combine(datetime.min, schedule.start_time) + timedelta(hours=1)).time()

            schedule.save()
            return Response({'status': 'success', 'message': 'Cập nhật thành công!'})

        except ClassSchedule.DoesNotExist:
            return Response({'status': 'error', 'message': 'Không tìm thấy môn học'}, status=status.HTTP_404_NOT_FOUND)
        except Exception as e:
            return Response({'status': 'error', 'message': str(e)}, status=status.HTTP_400_BAD_REQUEST)

class ProfileAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        return Response({
            'status': 'success',
            'full_name': user.full_name,
            'email': user.email,
            'phone_number': user.phone_number or '',
            'username': user.username,
        }, status=status.HTTP_200_OK)


class UpdateProfileAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def put(self, request):
        user = request.user

        full_name = request.data.get('full_name', '').strip()
        email = request.data.get('email', '').strip()
        phone_number = request.data.get('phone_number', '').strip()

        if not full_name:
            return Response({
                'status': 'error',
                'message': 'Họ và tên không được để trống'
            }, status=status.HTTP_400_BAD_REQUEST)

        user.full_name = full_name
        user.email = email
        user.phone_number = phone_number
        user.save()

        return Response({
            'status': 'success',
            'message': 'Cập nhật hồ sơ thành công',
            'full_name': user.full_name,
            'email': user.email,
            'phone_number': user.phone_number or '',
        }, status=status.HTTP_200_OK)


class ChangePasswordAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def post(self, request):
        user = request.user

        current_password = request.data.get('current_password', '').strip()
        new_password = request.data.get('new_password', '').strip()

        if not current_password or not new_password:
            return Response({
                'status': 'error',
                'message': 'Vui lòng nhập đầy đủ mật khẩu'
            }, status=status.HTTP_400_BAD_REQUEST)

        if not user.check_password(current_password):
            return Response({
                'status': 'error',
                'message': 'Mật khẩu hiện tại không đúng'
            }, status=status.HTTP_400_BAD_REQUEST)

        try:
            validate_password(new_password, user=user)
        except ValidationError as e:
            return Response({
                'status': 'error',
                'message': e.messages[0]
            }, status=status.HTTP_400_BAD_REQUEST)

        user.set_password(new_password)
        user.save()

        token, created = Token.objects.get_or_create(user=user)

        return Response({
            'status': 'success',
            'message': 'Đổi mật khẩu thành công',
            'token': token.key
        }, status=status.HTTP_200_OK)

# Tạo học kì mới
class SemesterAPIView(APIView):
    authentication_classes = [TokenAuthentication]
    permission_classes = [IsAuthenticated]

    def get(self, request):
        # Lấy học kỳ mới nhất của user
        semester = Semester.objects.filter(user=request.user).order_by('-id').first()
        if semester:
            return Response({
                'status': 'success',
                'id': semester.id,
                'name': semester.name,
                'start_date': semester.start_date.strftime('%d/%m/%Y'),
                'end_date': semester.end_date.strftime('%d/%m/%Y'),
            }, status=status.HTTP_200_OK)
        return Response({'status': 'not_found', 'message': 'Chưa có học kỳ'})

    def post(self, request):
        name = request.data.get('name')
        start_date_str = request.data.get('start_date')  # Android gửi lên dd/MM/yyyy
        end_date_str = request.data.get('end_date')

        if not name or not start_date_str or not end_date_str:
            return Response({'error': 'Thiếu thông tin'}, status=status.HTTP_400_BAD_REQUEST)

        try:
            start_date = datetime.strptime(start_date_str, '%d/%m/%Y').date()
            end_date = datetime.strptime(end_date_str, '%d/%m/%Y').date()

            semester = Semester.objects.create(
                user=request.user,
                name=name,
                start_date=start_date,
                end_date=end_date
            )
            return Response({
                'status': 'success',
                'message': 'Tạo học kỳ thành công',
                'id': semester.id,
                'name': semester.name,
                'start_date': start_date.strftime('%d/%m/%Y'),
                'end_date': end_date.strftime('%d/%m/%Y'),
            }, status=status.HTTP_201_CREATED)
        except Exception as e:
            return Response({'error': str(e)}, status=status.HTTP_400_BAD_REQUEST)