import React from "react";
import { useNavigate } from "react-router-dom";
import "./BackButton.css";

const BackButton = ({ onClick, text = "← 뒤로가기" }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    if (onClick) {
      onClick();
    } else {
      navigate("/user");
    }
  };

  return (
    <div className="back-button-wrapper">
      <div className="back-button-container">
        <button className="back-button" onClick={handleClick}>
          {text}
        </button>
      </div>
    </div>
  );
};

export default BackButton;


