# Manual Testing Guide - Peace App Enhancement

## Overview

This document provides guidance on how to conduct manual testing for the Peace app enhancements using the comprehensive checklist provided in `MANUAL_TESTING_CHECKLIST.md`.

## Purpose

Manual testing is essential to verify that:
1. All features work as intended in real-world scenarios
2. User experience is smooth and intuitive
3. Edge cases are handled gracefully
4. Performance is acceptable on target devices
5. Accessibility requirements are met
6. No regressions in existing functionality

## Testing Approach

### 1. Preparation

**Before starting testing:**
- [ ] Build the latest version of the app
- [ ] Install on test device(s)
- [ ] Clear app data for fresh start
- [ ] Prepare test data (images, sample reminders)
- [ ] Print or open `MANUAL_TESTING_CHECKLIST.md`
- [ ] Set up test environment (calendar account, etc.)

**Test Devices:**
- Minimum: 1 physical device (Android 8.0+)
- Recommended: 2-3 devices with different:
  - Screen sizes (small phone, large phone, tablet)
  - Android versions (8.0, 11, 13+)
  - Manufacturers (Samsung, Pixel, etc.)

### 2. Testing Phases

**Phase 1: Core Features (Priority 1)**
Test the most critical new features first:
1. Ionicons Integration (Section 1)
2. Subtasks and Checklists (Section 3)
3. Notifications (Section 14)
4. Peace Garden (Section 8)

**Phase 2: Customization (Priority 2)**
Test customization features:
1. Custom Fonts (Section 2)
2. Background Images (Section 5)
3. Custom Alarm Sounds (Section 6)
4. Language Selection (Section 7)

**Phase 3: Integration (Priority 3)**
Test integration features:
1. Calendar Sync (Section 9)
2. Deep Link Sharing (Section 10)
3. Widgets (Section 13)

**Phase 4: Intelligence (Priority 4)**
Test ML features:
1. ML Suggestions (Section 11)
2. Feature Toggles (Section 12)

**Phase 5: Quality (Priority 5)**
Test quality aspects:
1. Performance (Section 15)
2. Accessibility (Section 16)
3. Edge Cases (Section 18)
4. Device Compatibility (Section 19)
5. Regression (Section 20)

### 3. Testing Methodology

**For each test case:**

1. **Setup**: Prepare the necessary preconditions
2. **Execute**: Perform the test steps
3. **Verify**: Check the expected outcome
4. **Document**: Mark pass/fail and add notes

**Marking Results:**
- ✓ = Pass (feature works as expected)
- ✗ = Fail (feature doesn't work or has issues)
- N/A = Not applicable (feature not implemented)
- ? = Unclear (needs clarification)

**Adding Notes:**
- Document any issues found
- Include steps to reproduce bugs
- Note performance concerns
- Capture screenshots if helpful
- Record device/OS version for issues

### 4. Bug Reporting

**When you find a bug:**

1. **Verify it's reproducible**
   - Try to reproduce 2-3 times
   - Try on different devices if available

2. **Document thoroughly**
   - Title: Brief description
   - Steps to reproduce
   - Expected behavior
   - Actual behavior
   - Device/OS version
   - Screenshots/videos if applicable

3. **Classify severity**
   - **Critical**: App crashes, data loss, security issue
   - **High**: Major feature broken, poor UX
   - **Medium**: Minor feature issue, workaround exists
   - **Low**: Cosmetic issue, minor inconvenience

4. **Report**
   - Create GitHub issue (or your bug tracking system)
   - Link to test case in checklist
   - Assign priority and severity

### 5. Special Testing Scenarios

**Ionicons Testing:**
- Systematically go through each screen
- Check all icon locations listed in Section 1.4
- Verify no Material Icons remain
- Test theme switching for icon tinting

**Subtasks Testing:**
- Create reminders with 0, 1, 5, 10, 50 subtasks
- Test progress calculation at each level
- Verify performance with many subtasks

**Notifications Testing:**
- Test all three action buttons multiple times
- Test with nag mode enabled/disabled
- Test with multiple simultaneous notifications
- Test panic loop behavior thoroughly

**Calendar Sync Testing:**
- Test with no internet connection
- Test with permission denied
- Test with calendar app not installed
- Verify sync statistics accuracy

**Deep Link Testing:**
- Share via multiple apps (SMS, WhatsApp, Email)
- Test with app installed and not installed
- Test with various reminder configurations
- Verify data preservation

**ML Suggestions Testing:**
- Requires 7+ days of usage data
- Create patterns intentionally:
  - Complete tasks at same time daily
  - Create similar tasks repeatedly
  - Work in long focus sessions
- Wait for suggestions to generate
- Test apply/dismiss actions

**Widget Testing:**
- Add all three widget types
- Test on different launcher apps
- Verify updates after data changes
- Test click handling

### 6. Performance Testing

**Startup Time:**
- Measure with stopwatch
- Cold start: Force stop app, clear from recents, launch
- Warm start: Press home, relaunch from recents
- Target: <2s cold, <1s warm

**Memory Usage:**
- Use Android Studio Profiler
- Monitor during normal use
- Check for memory leaks
- Target: <200MB typical usage

**Battery Usage:**
- Use device battery settings
- Monitor over 24 hours
- Check for wakelocks
- Target: <5% daily battery usage

**Database Performance:**
- Create 100+ reminders
- Test scrolling performance
- Test search performance
- Target: No lag, smooth 60fps

### 7. Accessibility Testing

**TalkBack Testing:**
1. Enable TalkBack in device settings
2. Navigate through app using gestures
3. Verify all elements announced
4. Check navigation order logical
5. Test all actions accessible

**Font Scaling:**
1. Set device font size to largest
2. Verify all text visible
3. Check no truncation
4. Verify layout adapts

**Color Contrast:**
1. Use accessibility scanner tool
2. Check all text meets contrast ratios
3. Verify color not sole indicator

### 8. Regression Testing

**Critical to verify:**
- All existing reminder functionality works
- Nag mode still functions correctly
- Panic loop behavior unchanged
- Categories and priorities work
- History functionality intact
- Themes still work

### 9. Sign-off Criteria

**Before marking testing complete:**
- [ ] All critical test cases passed
- [ ] All high-priority bugs fixed
- [ ] Medium/low bugs documented and triaged
- [ ] Performance meets targets
- [ ] Accessibility requirements met
- [ ] No critical regressions
- [ ] Tested on minimum 2 devices
- [ ] Tested on minimum Android version (8.0)

### 10. Tips for Effective Testing

**Do:**
- Test with real-world scenarios
- Use the app as an actual user would
- Test edge cases and error conditions
- Take breaks to maintain focus
- Document everything
- Retest after bug fixes

**Don't:**
- Rush through test cases
- Skip "obvious" tests
- Test only happy paths
- Ignore minor issues
- Test only on one device
- Assume features work without verification

## Testing Schedule

**Recommended timeline:**
- Day 1-2: Phase 1 (Core Features)
- Day 3: Phase 2 (Customization)
- Day 4: Phase 3 (Integration)
- Day 5: Phase 4 (Intelligence)
- Day 6-7: Phase 5 (Quality)
- Day 8: Regression and final checks
- Day 9: Retest bug fixes
- Day 10: Final sign-off

**Total estimated time:** 40-60 hours

## Conclusion

Thorough manual testing is crucial for delivering a high-quality app. Use this guide and the comprehensive checklist to ensure all features work correctly and provide an excellent user experience.

Remember: The goal is not just to check boxes, but to verify that the Peace app truly delivers on its promise of "Calm Engagement" while providing powerful productivity features.

