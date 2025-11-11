# Feedback API - Correct Usage Guide

## Issue Resolved
Employee users were unable to submit feedback due to incorrect enum values being used.

## Required Fields for Feedback Submission

### POST /api/feedback

**Required Fields:**
- `userId` - ID of the user submitting feedback (get from login response)
- `driverId` - ID of the driver being reviewed
- `feedbackText` - The actual feedback (10-2000 characters)
- `rating` - Rating from 1-5
- `feedbackType` - Type of feedback (see valid types below)
- `source` - Source of feedback (see valid sources below)

### Valid FeedbackType Values

```java
EMPLOYEE              // Employee conduct, professionalism
DRIVING_SAFETY        // Speed, braking, route choice
VEHICLE_CONDITION     // Cleanliness, AC, seat comfort
ROUTE_NAVIGATION      // Route accuracy, delays
TRIP                  // Trip-specific feedback
MOBILE_APP            // App experience, bugs, features
MARSHAL               // Marshal conduct, helpfulness
GENERAL_EXPERIENCE    // Overall experience
POSITIVE_PRAISE       // Compliments and appreciation ✓ Use this for positive feedback
COMPLAINT             // Issues and problems
```

### Valid FeedbackSource Values

```java
MOBILE_APP
WEB_PORTAL     // ✓ Use this for web submissions
EMAIL
CALL_CENTER
SMS
OTHER
```

## Working Example

### 1. Login to get userId and token
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "testuser@moveinsync.com",
  "password": "TestPass123!"
}

Response:
{
  "data": {
    "token": "eyJhbGci...",
    "user": {
      "id": 72,
      "name": "Test User",
      "role": "EMPLOYEE"
    }
  }
}
```

### 2. Submit Feedback
```bash
POST http://localhost:8080/api/feedback
Authorization: Bearer eyJhbGci...
Content-Type: application/json

{
  "userId": 72,
  "driverId": 1,
  "feedbackText": "The driver was very professional and courteous. Great experience overall!",
  "rating": 5,
  "feedbackType": "POSITIVE_PRAISE",
  "source": "WEB_PORTAL"
}

Response:
{
  "success": true,
  "message": "Feedback submitted successfully",
  "data": {
    "id": 1,
    "driverId": 1,
    "userId": 72,
    "feedbackType": "POSITIVE_PRAISE",
    "feedbackText": "The driver was very professional...",
    "rating": 5,
    "source": "WEB_PORTAL",
    "status": "SUBMITTED",
    "createdAt": "2025-11-10T18:17:24.418643"
  }
}
```

## PowerShell Test Script

```powershell
# Login
$loginBody = '{"email":"testuser@moveinsync.com","password":"TestPass123!"}'
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
$token = $loginResponse.data.token
$userId = $loginResponse.data.user.id

# Submit Feedback
$headers = @{"Authorization"="Bearer $token"; "Content-Type"="application/json"}
$feedbackBody = @{
    userId = $userId
    driverId = 1
    feedbackText = "Great driver, very professional!"
    rating = 5
    feedbackType = "POSITIVE_PRAISE"
    source = "WEB_PORTAL"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/feedback" -Method POST -Headers $headers -Body $feedbackBody -ContentType "application/json"
$response.data | Format-List
```

## Common Errors and Solutions

### Error 1: "User ID is required"
**Problem:** Missing `userId` field  
**Solution:** Include userId from login response

### Error 2: "Source is required"
**Problem:** Missing `source` field  
**Solution:** Add `"source": "WEB_PORTAL"`

### Error 3: "No enum constant...FeedbackType.COMPLIMENT"
**Problem:** Invalid feedback type  
**Solution:** Use valid types like `POSITIVE_PRAISE`, `EMPLOYEE`, `COMPLAINT`

### Error 4: "No enum constant...FeedbackSource.WEB"
**Problem:** Invalid source value  
**Solution:** Use `WEB_PORTAL` not `WEB`

## Date Fixed
November 10, 2025

## Status
✅ Working - Employees can now submit feedback successfully
