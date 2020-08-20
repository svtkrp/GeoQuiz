package com.book.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {

    private static final String KEY_INDEX = "index";

    private static final String KEY_TRUE_FALSE_BUTTONS_ENABLED = "true_and_false_buttons_are_enabled";
    private static final String KEY_NEXT_BUTTON_ENABLED = "next_button_is_enabled";
    private static final String KEY_CORRECT_ANSWERS_COUNT = "correct_answers_count";
    private static final String KEY_INCORRECT_ANSWERS_COUNT = "incorrect_answers_count";

    private static final int REQUEST_CODE_CHEAT = 0;

    private static final String KEY_QUESTIONS_WERE_CHEATED = "questions_were_cheated";

    private static final int MAX_CHEATS = 3;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;
    private TextView mLeftoverCheatsCountTextView;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_london, true),
            new Question(R.string.question_ocean, true),
            new Question(R.string.question_africa, true),
            new Question(R.string.question_spb, false),
            new Question(R.string.question_india, false),
            new Question(R.string.question_moscow, false),
    };

    private int mCurrentIndex = 0;

    private boolean mTrueFalseButtonsIsEnabled = true;
    private boolean mNextButtonIsEnabled = false;
    private int mCorrectAnswersCount = 0;
    private int mIncorrectAnswersCount = 0;

    private boolean mIsCheater;

    private boolean[] mQuestionsWereCheated = new boolean[mQuestionBank.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);

            mTrueFalseButtonsIsEnabled = savedInstanceState.getBoolean(KEY_TRUE_FALSE_BUTTONS_ENABLED, true);
            mNextButtonIsEnabled = savedInstanceState.getBoolean(KEY_NEXT_BUTTON_ENABLED, false);

            mCorrectAnswersCount = savedInstanceState.getInt(KEY_CORRECT_ANSWERS_COUNT, 0);
            mIncorrectAnswersCount = savedInstanceState.getInt(KEY_INCORRECT_ANSWERS_COUNT, 0);

            mQuestionsWereCheated = savedInstanceState.getBooleanArray(KEY_QUESTIONS_WERE_CHEATED);
            if (mQuestionsWereCheated != null) {
                mIsCheater = mQuestionsWereCheated[mCurrentIndex];
            }
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setEnabled(mTrueFalseButtonsIsEnabled);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);

                setEnabledOfButtons(false);

                checkEnd();
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setEnabled(mTrueFalseButtonsIsEnabled);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);

                setEnabledOfButtons(false);

                checkEnd();
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setEnabled(mNextButtonIsEnabled);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = mQuestionsWereCheated[mCurrentIndex];
                updateQuestion();

                setEnabledOfButtons(true);

                updateLeftoverCheatsCount();
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this,
                        answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mLeftoverCheatsCountTextView = findViewById(R.id.leftover_cheats_count_text_view);
        updateLeftoverCheatsCount();

        updateQuestion();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);

        savedInstanceState.putBoolean(KEY_TRUE_FALSE_BUTTONS_ENABLED, mTrueFalseButtonsIsEnabled);
        savedInstanceState.putBoolean(KEY_NEXT_BUTTON_ENABLED, mNextButtonIsEnabled);

        savedInstanceState.putInt(KEY_CORRECT_ANSWERS_COUNT, mCorrectAnswersCount);
        savedInstanceState.putInt(KEY_INCORRECT_ANSWERS_COUNT, mIncorrectAnswersCount);

        savedInstanceState.putBooleanArray(KEY_QUESTIONS_WERE_CHEATED, mQuestionsWereCheated);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            if (mIsCheater) {
                return;
            }
            mQuestionsWereCheated[mCurrentIndex] = CheatActivity.wasAnswerShown(data);
            mIsCheater = mQuestionsWereCheated[mCurrentIndex];
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        String isCheaterMessage;
        if (mIsCheater) {
            isCheaterMessage = getResources().getString(R.string.judgment_toast);
        } else {
            isCheaterMessage = getResources().getString(R.string.good_fellow_toast);;
        }

        String isCorrectMessage;
        if (userPressedTrue == answerIsTrue) {
            isCorrectMessage = getResources().getString(R.string.correct_toast);
            mCorrectAnswersCount ++;
        } else {
            isCorrectMessage = getResources().getString(R.string.incorrect_toast);
            mIncorrectAnswersCount ++;
        }

        String separatorMessage = getResources().getString(R.string.separator);

        Toast.makeText(this, isCheaterMessage + separatorMessage + isCorrectMessage,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void setEnabledOfButtons(boolean nextButtonIsPressed) {
        if (nextButtonIsPressed) {
            mTrueFalseButtonsIsEnabled = true;
            mNextButtonIsEnabled = false;
        } else {
            mTrueFalseButtonsIsEnabled = false;
            mNextButtonIsEnabled = true;
        }
        mTrueButton.setEnabled(mTrueFalseButtonsIsEnabled);
        mFalseButton.setEnabled(mTrueFalseButtonsIsEnabled);
        mNextButton.setEnabled(mNextButtonIsEnabled);
    }

    private void checkEnd() {
        if (mCurrentIndex == mQuestionBank.length - 1) {
            double percent = (double)mCorrectAnswersCount /
                    (mCorrectAnswersCount + mIncorrectAnswersCount) * 100;

            int cheatedQuestionsCount = calculateCheatedQuestions();

            String message = String.format(getResources().getString(R.string.end_toast),
                    mCorrectAnswersCount, mIncorrectAnswersCount, percent, cheatedQuestionsCount);

            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.show();

            mCorrectAnswersCount = 0;
            mIncorrectAnswersCount = 0;
        }
    }

    private int calculateCheatedQuestions() {
        int count = 0;
        for (boolean questionWasCheated : mQuestionsWereCheated) {
            if (questionWasCheated) {
                count++;
            }
        }
        return count;
    }

    private void updateLeftoverCheatsCount() {
        int leftoverCheatsCount = MAX_CHEATS - calculateCheatedQuestions();
        mLeftoverCheatsCountTextView.setText(String.format((String)getResources()
                .getText(R.string.leftover_cheats_count_text), leftoverCheatsCount));

        if (leftoverCheatsCount <= 0) {
            mCheatButton.setEnabled(false);
        }
    }
}