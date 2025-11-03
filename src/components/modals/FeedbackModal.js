import React from 'react';
import { X } from 'lucide-react';
import { publicFeedbackAPI } from '../../services/api';
import FeedbackForm from '../forms/FeedbackForm';
import toast from 'react-hot-toast';
import './Modal.css';

const FeedbackModal = ({ isOpen, onClose }) => {
  const handleSubmit = async (formData) => {
    try {
      await publicFeedbackAPI.submitFeedback(formData);
      toast.success('Cảm ơn bạn đã gửi phản hồi! Chúng tôi sẽ xem xét và phản hồi sớm nhất.');
      onClose();
    } catch (error) {
      console.error('Error submitting feedback:', error);
      toast.error('Có lỗi xảy ra khi gửi phản hồi');
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
          <FeedbackForm
            baseUrl=""
            onSubmitted={handleSubmit}
            onCancel={onClose}
          />
        </div>
      </div>
    </div>
  );
};

export default FeedbackModal;
