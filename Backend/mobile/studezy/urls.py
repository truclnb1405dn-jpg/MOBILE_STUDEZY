from django.urls import path
from .views import LoginAPIView, RegisterAPIView, HomeSummaryAPIView, ClassesTodayAPIView, TopDeadlinesAPIView, ClassesByDateAPIView, DeadlinesByDateAPIView, ToggleDeadlineAPIView

urlpatterns = [
    path('api/login/', LoginAPIView.as_view(), name='api-login'),
    path('api/register/', RegisterAPIView.as_view(), name='api-register'),
    path('api/home-summary/', HomeSummaryAPIView.as_view(), name='api-home-summary'),
    path('api/classes-today/', ClassesTodayAPIView.as_view()),
    path('api/top-deadlines/', TopDeadlinesAPIView.as_view()),
    path('api/classes-by-date/', ClassesByDateAPIView.as_view()),
    path('api/deadlines-by-date/', DeadlinesByDateAPIView.as_view()),
    path('api/deadlines/<int:pk>/toggle/', ToggleDeadlineAPIView.as_view()),
]