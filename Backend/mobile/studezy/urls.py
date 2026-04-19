from django.urls import path
from .views import LoginAPIView, RegisterAPIView, HomeSummaryAPIView, ClassesTodayAPIView, TopDeadlinesAPIView, ClassesByDateAPIView, DeadlinesByDateAPIView, ToggleDeadlineAPIView, TasksAPIView, UpdateTaskStatusAPIView, EditTaskAPIView, DeleteTaskAPIView

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
]
