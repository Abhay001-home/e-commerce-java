import React from 'react';
import { Star } from 'lucide-react';

const RatingStars = ({ rating = 0, count = 0, interactive = false, onRatingChange }) => {
  const stars = [1, 2, 3, 4, 5];

  return (
    <div className="flex items-center gap-1.5">
      <div className="flex items-center">
        {stars.map((star) => (
          <button
            key={star}
            type={interactive ? 'button' : 'button'}
            disabled={!interactive}
            onClick={() => interactive && onRatingChange && onRatingChange(star)}
            className={`${interactive ? 'cursor-pointer transition-transform hover:scale-125' : 'cursor-default'}`}
          >
            <Star
              className={`w-4 h-4 ${
                star <= rating
                  ? 'fill-amber-400 text-amber-400'
                  : 'fill-slate-700 text-slate-600'
              }`}
            />
          </button>
        ))}
      </div>
      {count > 0 && (
        <span className="text-xs text-slate-400 font-medium">({count})</span>
      )}
    </div>
  );
};

export default RatingStars;
