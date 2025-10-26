import React, { useState } from 'react';
import { X, User, Mail, MessageSquare, Star } from 'lucide-react';
import { publicFeedbackAPI } from '../../services/api';
import toast from 'react-hot-toast';
import './Modal.css';

const FeedbackModal = ({ isOpen, onClose }) => {
  const [formData, setFormData] = useState({
    customerName: '',
    email: '',
    rating: 5,
    feedbackType: 'general',
    subject: '',
    message: ''
  });

  const [loading, setLoading] = useState(false);

  const feedbackTypes = [
    { value: 'general', label: 'Phản hồi chung' },
    { value: 'complaint', label: 'Khiếu nại' },
    { value: 'suggestion', label: 'Đề xuất cải tiến' },
    { value: 'compliment', label: 'Khen ngợi' },
    { value: 'technical', label: 'Hỗ trợ kỹ thuật' }
  ];

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleRatingChange = (rating) => {
    setFormData(prev => ({
      ...prev,
      rating
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.customerName || !formData.email || !formData.message) {
      toast.error('Vui lòng điền đầy đủ thông tin bắt buộc');
      return;
    }

    try {
      setLoading(true);
      await publicFeedbackAPI.submitFeedback(formData);
      toast.success('Cảm ơn bạn đã gửi phản hồi! Chúng tôi sẽ xem xét và phản hồi sớm nhất.');
      onClose();
      setFormData({
        customerName: '',
        email: '',
        rating: 5,
        feedbackType: 'general',
        subject: '',
        message: ''
      });
    } catch (error) {
      console.error('Error submitting feedback:', error);
      toast.error('Có lỗi xảy ra khi gửi phản hồi');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content feedback-modal">
        <div className="modal-header">
          <h2>Gửi phản hồi</h2>
          <button className="close-btn" onClick={onClose}>
            <X className="icon" />
          </button>
        </div>

        <div className="modal-body">
          <form onSubmit={handleSubmit} className="feedback-form">
            <div className="form-group">
              <label htmlFor="customerName">
                <User className="label-icon" />
                Họ và tên *
              </label>
              <input
                type="text"
                id="customerName"
                name="customerName"
                value={formData.customerName}
                onChange={handleInputChange}
                required
                placeholder="Nhập họ và tên của bạn"
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">
                <Mail className="label-icon" />
                Email *
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                placeholder="Nhập địa chỉ email"
              />
            </div>

            <div className="form-group">
              <label htmlFor="feedbackType">
                <MessageSquare className="label-icon" />
                Loại phản hồi
              </label>
              <select
                id="feedbackType"
                name="feedbackType"
                value={formData.feedbackType}
                onChange={handleInputChange}
              >
                {feedbackTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>
                <Star className="label-icon" />
                Đánh giá
              </label>
              <div className="rating-input">
                {[1, 2, 3, 4, 5].map(star => (
                  <button
                    key={star}
                    type="button"
                    className={`star-btn ${star <= formData.rating ? 'active' : ''}`}
                    onClick={() => handleRatingChange(star)}
                  >
                    <Star className="star-icon" />
                  </button>
                ))}
                <span className="rating-text">
                  {formData.rating === 1 && 'Rất không hài lòng'}
                  {formData.rating === 2 && 'Không hài lòng'}
                  {formData.rating === 3 && 'Bình thường'}
                  {formData.rating === 4 && 'Hài lòng'}
                  {formData.rating === 5 && 'Rất hài lòng'}
                </span>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="subject">
                Tiêu đề
              </label>
              <input
                type="text"
                id="subject"
                name="subject"
                value={formData.subject}
                onChange={handleInputChange}
                placeholder="Nhập tiêu đề phản hồi"
              />
            </div>

            <div className="form-group">
              <label htmlFor="message">
                <MessageSquare className="label-icon" />
                Nội dung phản hồi *
              </label>
              <textarea
                id="message"
                name="message"
                value={formData.message}
                onChange={handleInputChange}
                rows={6}
                required
                placeholder="Nhập nội dung phản hồi chi tiết..."
              />
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Hủy
              </button>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Đang gửi...' : 'Gửi phản hồi'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default FeedbackModal;
