import React from "react";
import { useNavigate } from "react-router-dom";
import "./PageHeader.css";

const PageHeader = ({
  title,
  subtitle,
  onBack,
  backText = "← 뒤로가기",
  showLogout = true,
}) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate("/user");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
    window.location.href = "/";
  };

  return (
    <div className="page-header-wrapper">
      <div className="page-header-container">
        <div className="page-header-content">
          {/* 왼쪽: 뒤로가기 버튼 */}
          <button className="page-header-back-button" onClick={handleBack}>
            {backText}
          </button>

          {/* 가운데: 제목 영역 */}
          <div className="page-header-title-area">
            <h1 className="page-header-title">{title}</h1>
            {subtitle && <p className="page-header-subtitle">{subtitle}</p>}
          </div>

          {/* 오른쪽: 로그아웃 버튼 */}
          {showLogout && (
            <button
              className="page-header-logout-button"
              onClick={handleLogout}
            >
              로그아웃
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PageHeader;

