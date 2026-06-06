import request from './index'

/**
 * Excel 导出 API 接口
 */
export const exportApi = {
  /** 导出一个工区的数据 */
  exportArea(areaId: number, periodId: number): Promise<Blob> {
    return request.get(`/export/area/${areaId}`, {
      params: { periodId },
      responseType: 'blob'
    })
  },

  /** 导出一个车间的数据 */
  exportWorkshop(workshopId: number, periodId: number): Promise<Blob> {
    return request.get(`/export/workshop/${workshopId}`, {
      params: { periodId },
      responseType: 'blob'
    })
  },

  /** 导出全段数据 */
  exportSection(periodId: number, workshopIds?: number[]): Promise<Blob> {
    return request.get('/export/section', {
      params: { periodId, workshopIds: workshopIds?.join(',') },
      responseType: 'blob'
    })
  }
}
