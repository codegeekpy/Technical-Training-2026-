from datetime import datetime
from typing import Optional
from pydantic import BaseModel, EmailStr, field_validator
from app.models.user import UserRole


class UserRegister(BaseModel):
    full_name: str
    email: EmailStr
    password: str
    department: Optional[str] = None
    role: Optional[UserRole] = UserRole.student

    @field_validator("email")
    @classmethod
    def must_be_university_email(cls, v):
        if not v.endswith("@anurag.edu.in"):
            raise ValueError("Only @anurag.edu.in email addresses are allowed")
        return v

    @field_validator("password")
    @classmethod
    def password_length(cls, v):
        if len(v) < 6:
            raise ValueError("Password must be at least 6 characters")
        return v


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class UserOut(BaseModel):
    id: int
    full_name: str
    email: str
    role: UserRole
    department: Optional[str]
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True


class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    department: Optional[str] = None


class UserRoleUpdate(BaseModel):
    role: UserRole


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut
