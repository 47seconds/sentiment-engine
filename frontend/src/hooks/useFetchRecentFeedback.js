import { useState, useEffect } from 'react';
import { getRecentFeedback } from '../services/feedbackService';

/**
 * Custom hook to fetch recent feedback
 * @param {number} limit - Number of recent items to fetch (default 10)
 * Returns { feedback, loading, error, refetch }
 */
export const useFetchRecentFeedback = (limit = 10) => {
  const [feedback, setFeedback] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getRecentFeedback(limit);
      
      // Handle both direct array and ApiResponse wrapper
      const data = response.data || response;
      
      if (Array.isArray(data)) {
        setFeedback(data);
      } else if (data.content) {
        // Spring Boot Page response
        setFeedback(data.content);
      } else {
        setFeedback([]);
      }
    } catch (err) {
      setError(err.message || 'Failed to fetch feedback');
      setFeedback([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [limit]);

  return {
    feedback,
    loading,
    error,
    refetch: fetchData,
  };
};

export default useFetchRecentFeedback;
