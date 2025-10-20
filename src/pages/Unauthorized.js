import React from 'react';
import { AlertCircle, Home } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import './Unauthorized.css';

const Unauthorized = () => {
  const navigate = useNavigate();

  return (
    <div className="unauthorized-container">
      <div className="unauthorized-content">
        <div className="unauthorized-icon">
          <AlertCircle size={64} />
        </div>
        <h1>Không có quyền truy cập</h1>
        <p>
          Bạn không có quyền truy cập vào trang này. Vui lòng liên hệ quản trị viên 
          để được cấp quyền phù hợp.
        </p>
        <button 
          className="btn btn-primary"
          onClick={() => navigate('/dashboard')}
        >
          <Home size={20} />
          Về trang chủ
        </button>
      </div>
    </div>
  );
};

export default Unauthorized;
