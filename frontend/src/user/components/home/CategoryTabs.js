import React from "react";
import "./CategoryTabs.css";

const CategoryTabs = ({ selectedCategory, onCategoryChange }) => {
  const categories = [
    { id: "hotel", name: "호텔" },
    { id: "nightview", name: "야경명소" },
    { id: "restaurant", name: "맛집" },
  ];

  return (
    <div className="category-tabs-container">
      {categories.map((category) => (
        <button
          key={category.id}
          className={`category-tab ${
            selectedCategory === category.id ? "active" : ""
          }`}
          onClick={() => onCategoryChange(category.id)}
        >
          {category.name}
        </button>
      ))}
    </div>
  );
};

export default CategoryTabs;


