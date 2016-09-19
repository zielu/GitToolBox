package zielu.gittoolbox.ui;

import com.google.common.collect.ImmutableMap;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;

public enum StatusPresenters implements StatusPresenter {
    arrows {
        @Override
        public String behindStatus(int behind) {
            return behind + UtfSeq.arrowDown;
        }

        @Override
        public String aheadBehindStatus(int ahead, int behind) {
            return ahead + UtfSeq.arrowUp + " " + behind + UtfSeq.arrowDown;
        }

        @Override
        public String nonZeroAheadBehindStatus(int ahead, int behind) {
            String aheadText = "";
            if (ahead > 0) {
                aheadText = ahead + UtfSeq.arrowUp;
            }
            String behindText = "";
            if (behind > 0) {
                behindText = behind + UtfSeq.arrowDown;
            }
            return format(aheadText, behindText);
        }

        @Override
        public String key() {
            return "arrows";
        }

        @Override
        public String getLabel() {
            return ResBundle.getString("presentation.label.arrows");
        }
    },
    arrowHeads {
        @Override
        public String behindStatus(int behind) {
            return behind + UtfSeq.arrowHeadDown;
        }

        @Override
        public String aheadBehindStatus(int ahead, int behind) {
            return ahead + UtfSeq.arrowHeadUp + " " + behind + UtfSeq.arrowHeadDown;
        }

        @Override
        public String nonZeroAheadBehindStatus(int ahead, int behind) {
            String aheadText = "";
            if (ahead > 0) {
                aheadText = ahead + UtfSeq.arrowHeadUp;
            }
            String behindText = "";
            if (behind > 0) {
                behindText = behind + UtfSeq.arrowHeadDown;
            }
            return format(aheadText, behindText);
        }

        @Override
        public String key() {
            return "arrowHeads";
        }

        @Override
        public String getLabel() {
            return ResBundle.getString("presentation.label.arrowHeads");
        }
    },
    text {
        @Override
        public String behindStatus(int behind) {
            return behind + " " + ResBundle.getString("git.behind");
        }

        @Override
        public String aheadBehindStatus(int ahead, int behind) {
            return ahead + " // " + behind;
        }

        @Override
        public String nonZeroAheadBehindStatus(int ahead, int behind) {
            if (ahead > 0 && behind > 0) {
                return aheadBehindStatus(ahead, behind);
            } else if (ahead > 0) {
                return ahead + " " + ResBundle.getString("git.ahead");
            } else if (behind > 0) {
                return behind + " " + ResBundle.getString("git.behind");
            } else {
                return "";
            }
        }

        @Override
        public String key() {
            return "text";
        }

        @Override
        public String getLabel() {
            return ResBundle.getString("presentation.label.text");
        }
    };

    private static final ImmutableMap<String, StatusPresenter> presenters;

    static {
        ImmutableMap.Builder<String, StatusPresenter> builder = ImmutableMap.builder();
        for (StatusPresenters presenter : values()) {
            builder.put(presenter.key(), presenter);
        }
        presenters = builder.build();
    }

    public static StatusPresenter forKey(String key) {
        return presenters.get(key);
    }

    private static String format(String aheadText, String behindText) {
        if (aheadText.length() > 0 && behindText.length() > 0) {
            return aheadText + " " + behindText;
        } else if (aheadText.length() == 0 && behindText.length() == 0) {
            return "";
        } else {
            if (aheadText.length() > 0) {
                return aheadText;
            } else {
                return behindText;
            }
        }
    }
}
