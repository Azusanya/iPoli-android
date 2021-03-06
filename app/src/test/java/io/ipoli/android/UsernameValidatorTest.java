package io.ipoli.android;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.app.ui.UsernameValidator;
import io.ipoli.android.feed.persistence.FeedPersistenceService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class UsernameValidatorTest {

    @Mock
    private FeedPersistenceService defaultFeedPersistenceService;

    @Mock
    private FeedPersistenceService notUniqueFeedPersistenceService;

    @Mock
    private UsernameValidator.ResultListener resultListener;

    @Before
    public void setUp() {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                OnDataChangedListener<Boolean> dataChangedListener = invocationOnMock.getArgument(1);
                dataChangedListener.onDataChanged(true);
                return null;
            }
        }).when(defaultFeedPersistenceService).isUsernameAvailable(anyString(), any(OnDataChangedListener.class));


        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                OnDataChangedListener<Boolean> dataChangedListener = invocationOnMock.getArgument(1);
                dataChangedListener.onDataChanged(false);
                return null;
            }
        }).when(notUniqueFeedPersistenceService).isUsernameAvailable(anyString(), any(OnDataChangedListener.class));
    }

    @Test
    public void validate_validUsername_callOnValid() {
        UsernameValidator.validate("iPoli", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onValid();
    }

    @Test
    public void validate_nullUsername_callOnInvalidWithEmptyError() {
        UsernameValidator.validate(null, defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.EMPTY);
    }

    @Test
    public void validate_emptyUsername_callOnInvalidWithEmptyError() {
        UsernameValidator.validate("", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.EMPTY);
    }

    @Test
    public void validate_notUniqueUsername_callOnInvalidWithNotUniqueError() {
        UsernameValidator.validate("iPoli", notUniqueFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.NOT_UNIQUE);
    }

    @Test
    public void validate_usernameWithSpace_callOnInvalidWithFormatError() {
        UsernameValidator.validate("i Poli", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.FORMAT);
    }

    @Test
    public void validate_usernameWithSpecialCharacter_callOnInvalidWithFormatError() {
        UsernameValidator.validate("i*Poli", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.FORMAT);
    }

    @Test
    public void validate_usernameWithPunctuationMark_callOnInvalidWithFormatError() {
        UsernameValidator.validate("i.Poli", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.FORMAT);
    }

    @Test
    public void validate_usernameWithUnderscore_callOnValid() {
        UsernameValidator.validate("i_Poli", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onValid();
    }

    @Test
    public void validate_usernameWithBulgarianChars_callOnInvalidWithFormatError() {
        UsernameValidator.validate("AsГуйчо1", defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.FORMAT);
    }

    @Test
    public void validate_tooShortUsername_callOnInvalidWithLengthError() {
        UsernameValidator.validate(generateUsername(UsernameValidator.MIN_LENGTH - 1), defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.LENGTH);
    }

    @Test
    public void validate_tooLongUsername_callOnInvalidWithLengthError() {
        UsernameValidator.validate(generateUsername(UsernameValidator.MAX_LENGTH + 1), defaultFeedPersistenceService, resultListener);
        verify(resultListener).onInvalid(UsernameValidator.UsernameValidationError.LENGTH);
    }

    private String generateUsername(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
