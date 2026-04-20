from django.urls import path
from .views import LoginAPIView, RegisterAPIView, HomeSummaryAPIView, ClassesTodayAPIView, TopDeadlinesAPIView, ClassesByDateAPIView, DeadlinesByDateAPIView, ToggleDeadlineAPIView, TasksAPIView, UpdateTaskStatusAPIView, EditTaskAPIView, DeleteTaskAPIView
from .views import AddClassScheduleAPIView, AllClassSchedulesAPIView, ClassScheduleDetailAPIView, ProfileAPIView, UpdateProfileAPIView, ChangePasswordAPIView

urlpatterns = [
    path('api/login/', LoginAPIView.as_view(), name='api-login'),
    path('api/register/', RegisterAPIView.as_view(), name='api-register'),
    path('api/home-summary/', HomeSummaryAPIView.as_view(), name='api-home-summary'),
    path('api/classes-today/', ClassesTodayAPIView.as_view()),
    path('api/top-deadlines/', TopDeadlinesAPIView.as_view()),
    path('api/classes-by-date/', ClassesByDateAPIView.as_view()),
    path('api/deadlines-by-date/', DeadlinesByDateAPIView.as_view()),
    path('api/deadlines/<int:pk>/toggle/', ToggleDeadlineAPIView.as_view()),
    path('api/tasks/', TasksAPIView.as_view(), name='api-tasks'),
    path('api/tasks/<int:pk>/update-status/', UpdateTaskStatusAPIView.as_view(), name='api-update-task-status'),
    path('api/tasks/<int:pk>/edit/', EditTaskAPIView.as_view(), name='api-edit-task'),
    path('api/tasks/<int:pk>/delete/', DeleteTaskAPIView.as_view(), name='api-delete-task'),
    path('api/add-class-schedule/', AddClassScheduleAPIView.as_view(), name='add-class-schedule'),
    path('api/class-schedules/', AllClassSchedulesAPIView.as_view(), name='api-class-schedules'),
    path('api/class-schedules/<int:pk>/',ClassScheduleDetailAPIView.as_view()),
    path('api/profile/', ProfileAPIView.as_view(), name='api-profile'),
    path('api/update-profile/', UpdateProfileAPIView.as_view(), name='api-update-profile'),
    path('api/change-password/', ChangePasswordAPIView.as_view(), name='api-change-password'),
]
