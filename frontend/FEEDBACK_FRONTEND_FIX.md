# Feedback Frontend Fix

## Issue
Frontend feedback submission was failing because it wasn't sending all required fields to the backend API.

## Root Cause
The `FeedbackPage.jsx` was sending:
```javascript
{
  entityType,
  entityId: finalEntityId,
  feedbackText,
  rating,
  source
}
```

But the backend `/api/feedback` endpoint requires:
```javascript
{
  userId,        // ❌ MISSING
  driverId,      // ❌ MISSING (for EMPLOYEE feedback)
  feedbackType,  // ❌ MISSING
  feedbackText,  // ✓
  rating,        // ✓
  source         // ✓
}
```

## Solution Applied

### Updated `FeedbackPage.jsx` handleSubmit function:

**Added:**
1. `userId: user?.id` - Get from authenticated user context
2. `driverId` - Set when entityType is EMPLOYEE
3. `feedbackType` - Intelligently determined based on rating and entityType:
   - Rating ≥ 4 → `POSITIVE_PRAISE`
   - Rating ≤ 2 → `COMPLAINT`
   - Otherwise → Use entity-specific type (`EMPLOYEE`, `TRIP`, `MOBILE_APP`, `MARSHAL`, or `GENERAL_EXPERIENCE`)

### Complete Fixed Payload:
```javascript
const feedbackData = {
  userId: user?.id,                              // ✓ Added
  driverId: entityType === 'EMPLOYEE' ? finalEntityId : null,  // ✓ Added
  entityType,                                    // ✓ Existing
  entityId: finalEntityId,                       // ✓ Existing
  feedbackText: feedbackText.trim(),            // ✓ Existing
  rating: rating || null,                        // ✓ Existing
  feedbackType,                                  // ✓ Added (smart mapping)
  source,                                        // ✓ Existing
};
```

## FeedbackType Mapping Logic

The fix includes intelligent mapping:
```javascript
if (rating >= 4) {
  feedbackType = 'POSITIVE_PRAISE';
} else if (rating <= 2) {
  feedbackType = 'COMPLAINT';
} else if (entityType === 'EMPLOYEE') {
  feedbackType = 'EMPLOYEE';
} else if (entityType === 'TRIP') {
  feedbackType = 'TRIP';
} else if (entityType === 'MOBILE_APP') {
  feedbackType = 'MOBILE_APP';
} else if (entityType === 'MARSHAL') {
  feedbackType = 'MARSHAL';
} else {
  feedbackType = 'GENERAL_EXPERIENCE';
}
```

## Valid Backend Enum Values

### FeedbackType (from backend):
- `EMPLOYEE` - Employee conduct, professionalism
- `DRIVING_SAFETY` - Speed, braking, route choice
- `VEHICLE_CONDITION` - Cleanliness, AC, seat comfort
- `ROUTE_NAVIGATION` - Route accuracy, delays
- `TRIP` - Trip-specific feedback
- `MOBILE_APP` - App experience, bugs, features
- `MARSHAL` - Marshal conduct, helpfulness
- `GENERAL_EXPERIENCE` - Overall experience
- `POSITIVE_PRAISE` - Compliments ✓ Used for high ratings
- `COMPLAINT` - Issues ✓ Used for low ratings

### FeedbackSource (from backend):
- `MOBILE_APP`
- `WEB_PORTAL` ✓ Default in frontend
- `EMAIL`
- `CALL_CENTER`
- `SMS`
- `OTHER`

## Testing

### Frontend is running on:
- Port: 3001 (3000 was in use)
- URL: http://localhost:3001

### To test:
1. Login as any user (employee, admin, etc.)
2. Navigate to Feedback page
3. Select a driver (for EMPLOYEE feedback)
4. Enter feedback text (min 10 characters)
5. Select rating (optional)
6. Click Submit

### Expected Result:
✅ Feedback submitted successfully
✅ No validation errors
✅ Feedback appears in recent feedback list
✅ Driver stats updated (if applicable)

## Files Modified
- `frontend/src/pages/FeedbackPage.jsx` - Added userId, driverId, and feedbackType mapping

## Date Fixed
November 10, 2025

## Status
✅ **FIXED** - Frontend now sends all required fields with proper enum values
