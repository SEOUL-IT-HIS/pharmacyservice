package kr.co.seoulit.his.pharmacyservice.common.commoncode;

/** admin-service GET /api/admin/commonCodeGroup/list 응답 한 행 */
record CommonCodeGroupDto(String groupId, String groupCode, String groupName, String useYn) {
}
