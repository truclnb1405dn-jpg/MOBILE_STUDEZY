from django.urls import path
from .views import LoginAPIView, RegisterAPIView, HomeSummaryAPIView, ClassesTodayAPIView, TopDeadlinesAPIView

urlpatterns = [
    path('api/login/', LoginAPIView.as_view(), name='api-login'),
    path('api/register/', RegisterAPIView.as_view(), name='api-register'),
    path('api/home-summary/', HomeSummaryAPIView.as_view(), name='api-home-summary'),
    path('api/classes-today/', ClassesTodayAPIView.as_view()),
    path('api/top-deadlines/', TopDeadlinesAPIView.as_view()),
]