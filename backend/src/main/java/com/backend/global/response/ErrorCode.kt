package com.backend.global.response

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val status: HttpStatus,
    val message: String
) {
    // 회원 (Member)
    DUPLICATE_MEMBER_ID("M003", HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("M004", HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME("M005", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    MEMBER_NOT_FOUND("M006", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD("M007", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ALREADY_DELETED_MEMBER("M008", HttpStatus.BAD_REQUEST, "이미 탈퇴된 회원입니다."),
    INVALID_MEMBER("M009", HttpStatus.UNAUTHORIZED, "회원 정보가 유효하지 않습니다."),

    //계획
    NOT_FOUND_PLAN("D001", HttpStatus.NOT_FOUND, "계획이 없습니다."),
    NOT_VALID_DATE("D002", HttpStatus.BAD_REQUEST, "입력된 날짜가 유효하지 않습니다."),
    NOT_SAME_MEMBER("D003", HttpStatus.FORBIDDEN, "본인이 작성한 계획만 수정이 가능합니다."),
    NOT_MY_PLAN("D004", HttpStatus.FORBIDDEN, "내가 만든 계획이 아닙니다."),

    //계획 상세
    NOT_FOUND_DETAIL_PLAN("D101", HttpStatus.NOT_FOUND, "상세 계획을 찾을 수 없습니다"),
    NOT_ALLOWED_MEMBER("D102", HttpStatus.UNAUTHORIZED, "허용되지 않은 사용자입니다."),
    NOT_ACCEPTED_MEMBER("D103", HttpStatus.FORBIDDEN, "초대를 승낙해야 사용이 가능합니다."),
    CONFLICT_TIME("D104", HttpStatus.CONFLICT, "겹치는 시간이 존재합니다. 겹치지 않는 시간으로 작성해 주세요."),

    //회원 초대
    NOT_FOUND_INVITE("I001", HttpStatus.NOT_FOUND, "초대 내역을 찾을 수 없습니다."),
    DUPLICATE_MEMBER_INVITE("I002", HttpStatus.CONFLICT, "이미 초대된 사용자입니다."),

    // 여행지
    NOT_FOUND_PLACE("P001", HttpStatus.NOT_FOUND, "여행지를 찾을 수 없습니다."),
    NOT_FOUND_CATEGORY("P002", HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    //북마크
    ALREADY_EXISTS_BOOKMARK("B001", HttpStatus.CONFLICT, "이미 북마크된 장소입니다."),
    NOT_FOUND_BOOKMARK("B002", HttpStatus.NOT_FOUND, "북마크를 찾을 수 없습니다."),
    FORBIDDEN_BOOKMARK("B003", HttpStatus.FORBIDDEN, "북마크에 대한 권한이 없습니다."),

    //리뷰
    NOT_FOUND_REVIEW("R001", HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    GIVEN_REVIEW("R002", HttpStatus.NOT_FOUND, "이미 별점을 남겼습니다."),

    // 인증/인가
    TOKEN_NOT_FOUND("A005", HttpStatus.UNAUTHORIZED, "요청에 토큰이 존재하지 않습니다."),

    UNAUTHORIZED_REQUEST("A015", HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다."),
    ACCESS_DENIED("A016", HttpStatus.FORBIDDEN, "인가되지 않은 요청입니다."),

    INACTIVE_MEMBER("A017", HttpStatus.FORBIDDEN, "비활성화된 계정입니다. 로그인할 수 없습니다."),


    UNAUTHORIZED_MEMBER("A006", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    LOGOUT_USER("A007", HttpStatus.UNAUTHORIZED, "로그아웃된 사용자입니다."),
    TOKEN_MALFORMED("A013", HttpStatus.BAD_REQUEST, "잘못된 형식의 JWT 토큰입니다."),
    BLACKLISTED_TOKEN("A014", HttpStatus.UNAUTHORIZED, "사용이 제한된 토큰입니다."),
    TOKEN_SIGNATURE_INVALID("A012", HttpStatus.UNAUTHORIZED, "토큰 서명이 유효하지 않습니다."),

    INVALID_REFRESH_TOKEN("A003", HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    EXPIRED_REFRESH_TOKEN("A008", HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    MISMATCH_REFRESH_TOKEN("A004", HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰과 일치하지 않습니다."),

    INVALID_ACCESS_TOKEN("A010", HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN("A009", HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),

    INVALID_TOKEN("A009",HttpStatus.UNAUTHORIZED,  "유효하지 않은 임시 토큰입니다."),

    // OAuth Provider
    UNSUPPORTED_OAUTH_PROVIDER("A018", HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth Provider입니다.");

}
