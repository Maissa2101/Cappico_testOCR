package androidhough.rbfntest;

public final class Exercise {
    public final String modelFileAbsolutePath;
    public final String answerFileAbsolutePath;
    private Boolean valid;

    public Exercise(String modelFileAbsolutePath, String answerFileAbsolutePath) {
        this.modelFileAbsolutePath = modelFileAbsolutePath;
        this.answerFileAbsolutePath = answerFileAbsolutePath;
    }
    public Exercise(String modelFileAbsolutePath, String answerFileAbsolutePath, boolean valid) {
        this.modelFileAbsolutePath = modelFileAbsolutePath;
        this.answerFileAbsolutePath = answerFileAbsolutePath;
        this.valid = valid;
    }

    public boolean isValid() {
        if(valid == null) throw new RuntimeException("This exercise validation was not specified yet.");
        return valid;
    }

    public void setValid(boolean valid) { this.valid = valid; }

    public String getModelFileAbsolutePath() {
        return modelFileAbsolutePath;
    }

    public String getAnswerFileAbsolutePath() {
        return answerFileAbsolutePath;
    }


    @Override
    public String toString() {
        return "Exercise{" +
                "modelFileAbsolutePath='" + modelFileAbsolutePath + '\'' +
                ", answerFileAbsolutePath='" + answerFileAbsolutePath + '\'' +
                ", valid=" + valid +
                '}';
    }
}
