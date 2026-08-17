export type Stage = 'ONLINE_PHONE' | 'ON_SITE' | 'PRE_CONTRACT'
export type ItemType = 'CORE' | 'OPTIONAL'
export type CheckStatus = 'UNCONFIRMED' | 'GOOD' | 'CAUTION'
export interface ApiEnvelope<T> { code: string; message: string; data: T }
export interface TokenPair { accessToken: string; refreshToken: string; tokenType: string; expiresIn: number }
export interface Member { memberId: number; name: string; email: string }
export interface LoginResult extends TokenPair { member: Member }
export interface SystemItem { checkItemId: number; stage: Stage; type: ItemType; question: string; guide: string | null }
export interface ChecklistItem extends SystemItem { checklistItemId: number; displayOrder: number }
export interface ChecklistSummary { checklistId: number; name: string; stage: Stage; itemCount: number; appliedPropertyCount: number }
export interface ChecklistDetail extends ChecklistSummary { items: ChecklistItem[]; createdAt?: string; updatedAt?: string }
export interface Progress { totalCount: number; completedCount: number; goodCount: number; cautionCount: number; unconfirmedCount: number; progressPercent: number }
export interface PropertyItem { propertyId: number; name: string; depositAmount: number | null; monthlyRentAmount: number | null; maintenanceFeeAmount: number | null; address: string | null; discoverySource: string | null; lastActivityAt: string; createdAt: string; updatedAt: string; progress: Progress }
export interface Page<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number; hasNext: boolean }
export interface MemoItem { label: string; content: string; order?: number }
export interface PropertyMemo { items: MemoItem[]; freeMemo: string; savedAt: string | null }
export interface AppliedItem { itemId: number; sourceCheckItemId: number; question: string; guide: string | null; displayOrder: number; status: CheckStatus; memo: string }
export interface AppliedSummary { propertyChecklistId: number | null; name: string | null; stage: Stage; applied: boolean; progress: Progress }
export interface AppliedDetail { propertyChecklistId: number; sourceChecklistId: number; name: string; stage: Stage; items: AppliedItem[]; progress: Progress }
