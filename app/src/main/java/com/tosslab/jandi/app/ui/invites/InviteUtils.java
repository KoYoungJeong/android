package com.tosslab.jandi.app.ui.invites;

///**
// * Created by tonyjs on 15. 5. 5..
// */
//@Deprecated
//public class InviteUtils {
//    private static final String PACKAGE_NAME_KAKAO = "com.kakao.talk";
//    private static final String PACKAGE_NAME_LINE = "jp.naver.line.android";
//    private static final String PACKAGE_NAME_WECHAT = "com.tencent.mm";
//    private static final String FACEBOOK_EXTRA_PROTOCOL_VERSION = "com.facebook.orca.extra.PROTOCOL_VERSION";
//    private static final String FACEBOOK_EXTRA_APP_ID = "com.facebook.orca.extra.APPLICATION_ID";
//    private static final int FACEBOOK_PROTOCOL_VERSION = 20150314;
//    private static final String FACEBOOK_REGISTRATION_APP_ID = "808900692521335";
//    private static final String PACKAGE_NAME_FACEBOOK_MESSENGER = "com.facebook.orca";
//
//    public enum Result {
//        UNDEFINED_URL, INVITATION_DISABLED, NETWORK_ERROR, ERROR, SUCCESS
//    }
//
//    public static Pair<Result, ResTeamDetailInfo.InviteTeam> checkInvitationDisabled(
//            TeamDomainInfoModel teamDomainInfoModel, int teamId) {
//        try {
//
//            ResTeamDetailInfo.InviteTeam inviteTeam = teamDomainInfoModel.getTeamInfo(teamId);
//
//            String invitationUrl = inviteTeam.getInvitationUrl();
//            if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
//                return new Pair<Result, ResTeamDetailInfo.InviteTeam>(Result.UNDEFINED_URL, null);
//            }
//
//            String invitationStatus = inviteTeam.getInvitationStatus();
//            // disabled
//            if (TextUtils.isEmpty(invitationStatus)
//                    || TextUtils.equals(invitationStatus, "disabled")) {
//                return new Pair<Result, ResTeamDetailInfo.InviteTeam>(Result.INVITATION_DISABLED, null);
//            }
//            return new Pair<Result, ResTeamDetailInfo.InviteTeam>(Result.SUCCESS, inviteTeam);
//        } catch (JandiNetworkException e) {
//            e.printStackTrace();
//            return new Pair<Result, ResTeamDetailInfo.InviteTeam>(Result.NETWORK_ERROR, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Pair<Result, ResTeamDetailInfo.InviteTeam>(Result.ERROR, null);
//        }
//    }
//
////    public static Intent getInviteIntent(Context activity, TeamInvitationsEvent event,
////                                         String invitationUrl, String invitationContents) {
////        switch (event.type) {
////            case JandiConstants.TYPE_INVITATION_KAKAO:
////                return getInviteIntent(invitationUrl, invitationContents,
////                        PACKAGE_NAME_KAKAO, false);
////            case JandiConstants.TYPE_INVITATION_LINE:
////                return getInviteIntent(invitationUrl, invitationContents,
////                        PACKAGE_NAME_LINE, false);
////            case JandiConstants.TYPE_INVITATION_WECHAT:
////                return getInviteIntent(invitationUrl, invitationContents,
////                        PACKAGE_NAME_WECHAT, false);
////            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
////                return getInviteIntent(invitationUrl, invitationContents,
////                        PACKAGE_NAME_FACEBOOK_MESSENGER, true);
////            default:
////            case JandiConstants.TYPE_INVITATION_EMAIL:
////                return InviteEmailActivity_
////                        .intent(activity)
////                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).get();
////        }
////    }
//
//    public static Intent getInviteIntent(Context activity, int eventType,
//                                         String invitationUrl, String invitationContents) {
//        switch (eventType) {
//            case JandiConstants.TYPE_INVITATION_KAKAO:
//                return getInviteIntent(invitationUrl, invitationContents,
//                        PACKAGE_NAME_KAKAO, false);
//            case JandiConstants.TYPE_INVITATION_LINE:
//                return getInviteIntent(invitationUrl, invitationContents,
//                        PACKAGE_NAME_LINE, false);
//            case JandiConstants.TYPE_INVITATION_WECHAT:
//                return getInviteIntent(invitationUrl, invitationContents,
//                        PACKAGE_NAME_WECHAT, false);
//            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
//                return getInviteIntent(invitationUrl, invitationContents,
//                        PACKAGE_NAME_FACEBOOK_MESSENGER, true);
//            default:
//            case JandiConstants.TYPE_INVITATION_EMAIL:
//                return InviteEmailActivity_
//                        .intent(activity)
//                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP).get();
//        }
//    }
//
//    private static Intent getInviteIntent(String publicLink, String contents,
//                                          String packageName, boolean isFacebookMessenger) {
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setPackage(packageName);
//        intent.putExtra(Intent.EXTRA_TEXT, contents + "\n" + publicLink);
//        intent.setType("text/plain");
//
//        if (isFacebookMessenger) {
//            intent.putExtra(FACEBOOK_EXTRA_PROTOCOL_VERSION, FACEBOOK_PROTOCOL_VERSION);
//            intent.putExtra(FACEBOOK_EXTRA_APP_ID, FACEBOOK_REGISTRATION_APP_ID);
//            intent.setType("image/*");
//        }
//        return intent;
//    }
//
//}
