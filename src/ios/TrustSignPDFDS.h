//
//  TrustSignPDFDS.h
//  TrustSignPDFDS
//
//  Created by WangLi on 2016/10/31.
//  Copyright © 2016年 CFCA. All rights reserved.
//
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#ifndef _PDF_BOOKVIEW_
#define _PDF_BOOKVIEW_

#pragma mark - TrustSignPDFTapInfo functions
typedef NS_ENUM(NSInteger, PDFTapAreaType) {
    PDFTapAreaTypeDefault           = 0,
    PDFTapAreaTypeSignaturedWidget  = 1,
    PDFTapAreaTypeReservedArea      = 2,
    PDFTapAreaTypeInvalid           = 3
};

@interface PDFTapAreaInfo : NSObject

@property (nonatomic, assign, readonly)PDFTapAreaType tapAreaType;

@property (nonatomic, assign, readonly)CGPoint      tapPointInBookView;
@property (nonatomic, assign, readonly)CGPoint      tapPointInPDF;//point in pdf page, not in bookview
@property (nonatomic, assign, readonly)CGRect       widgetFrame;  //rect in pdf page, not in bookview
@property (nonatomic, assign, readonly)NSUInteger   pageNo;       //from 0 start

@end

#pragma mark - TrustSignPDFPageInfo functions
@interface PDFPageInfo : NSObject

@property (nonatomic, assign, readonly)CGFloat    zoomScale;
@property (nonatomic, assign, readonly)CGRect     pdfImageFrame;//Relative to bookview
@property (nonatomic, assign, readonly)CGSize     pdfOriginalPageSize;
@property (nonatomic, assign, readonly)NSUInteger pageNo;//from 0 start

@end
#endif

#ifndef _PDF_DOCUMENT_SEAL_
#define _PDF_DOCUMENT_SEAL_

typedef NS_ENUM(NSUInteger, PDFVerifySealResult) {
    PDFVerifySealResultUnknown              = 0,
    PDFVerifySealResultSuccess              = 1,
    PDFVerifySealResultAbnormal             = 2,
    PDFVerifySealResultIsWedgetReservedArea = 3,
    PDFVerifySealResultFailure              = 4
};

typedef NS_ENUM(NSInteger, PDFSealSignatureStatus) {
    PDFSealSignatureStatusUnsigned           = -2,
    PDFSealSignatureStatusUnsupported_filter = -1,
    PDFSealSignatureStatusInvalid            = 0,
    PDFSealSignatureStatusValid              = 1
};

typedef NS_OPTIONS(NSInteger, PDFSealCertStatusMask) {
    PDFSealCertStatusError                = -1,
    PDFSealCertStatusOK                   = 0,
    PDFSealCertStatusMaskTimeInvalid      = (1 << 1),
    PDFSealCertStatusMaskCertChainInvalid = (1 << 2),
    PDFSealCertStatusMaskCrlInvalid       = (1 << 4)
};

typedef NS_OPTIONS(NSUInteger, PDFSealTimestampStatusMask) {
    PDFSealTimestampStatusOK              = 0,
    PDFSealTimestampStatusMaskSignInvalid = (1 << 0),
    PDFSealTimestampStatusMaskCertInvalid = (1 << 1),
    PDFSealTimestampStatusMaskHashInvalid = (1 << 2)
};

@interface PDFSealSignatureInfo : NSObject

@property (nonatomic, assign, readonly) PDFVerifySealResult   verifySealResult;

@property (nonatomic, assign, readonly)NSUInteger pageNo;
@property (nonatomic, assign, readonly)CGRect     widgetRectInPDF;

@property (nonatomic, strong, readonly) NSString  *reason;
@property (nonatomic, strong, readonly) NSString  *date;
@property (nonatomic, strong, readonly) NSString  *location;
@property (nonatomic, strong, readonly) NSString  *contact;
@property (nonatomic, strong, readonly) NSData    *contents;
@property (nonatomic, strong, readonly) NSData    *cert;
@property (nonatomic, assign, readonly) PDFSealSignatureStatus signatureStatus;
@property (nonatomic, assign, readonly) PDFSealCertStatusMask  certStatus;

@property (nonatomic, assign, readonly) BOOL      existTimestamp;
@property (nonatomic, assign, readonly) PDFSealTimestampStatusMask timestampStatus;
@property (nonatomic, assign, readonly) time_t    timestampTime;
@property (nonatomic, strong, readonly) NSString  *timestampTimeStr;
@property (nonatomic, strong, readonly) NSString  *tsaCN;
@property (nonatomic, strong, readonly) NSData    *tsaCert;

@end

#pragma mark - PDFSealInfoData
/*!
 @enum       PDFSealType
 @abstract   seal type, include 2 types.
 */
typedef NS_ENUM(NSUInteger, PDFSealType) {
    PDFSealTypeInvisible = 0,
    PDFSealTypeImage     = 1
};

/*!
 @enum       PDFSealCipherType
 @abstract   seal cipher type, include 3 types.
 */
typedef NS_ENUM(NSUInteger, PDFSealCipherType) {
    PDFSealCipherTypeRSA1024 = 0,
    PDFSealCipherTypeRSA2048 = 1,
    PDFSealCipherTypeSM2     = 2
};

/*!
 @enum       PDFSealHashType
 @abstract   hash type, include 3 types.
 */
typedef NS_ENUM(NSUInteger, PDFSealHashType) {
    PDFSealHashTypeSHA1    = 0,
    PDFSealHashTypeSHA256  = 1,
    PDFSealHashTypeSHA384  = 2,
    PDFSealHashTypeSM3     = 3
};

/*!
 @enum       PDFSignResult
 @abstract   sign result type, include 4 types.
 */
typedef NS_ENUM(NSUInteger, PDFSealResult) {
    PDFSignResultSucess          = 0,
    PDFSignResultInvalidPoint    = 1,
    PDFSignResultException       = 2,
    PDFSignResultFailed          = 3,
    PDFSignResultTimestampFailed = 4
};

/*!
 @enum       PDFSealAreaType
 @abstract   seal area type, include 2 types.
 PDFSealAreaTypeFix: fixed area to seal
 PDFSealAreaTypeCustom: seal in anywhere
 */
typedef NS_ENUM(NSUInteger, PDFSealAreaType) {
    PDFSealAreaTypeFix    = 0,
    PDFSealAreaTypeCustom = 1
};

@interface PDFSealInfo : NSObject

@property (nonatomic, strong)NSString  *reason;
@property (nonatomic, strong)NSString  *location;
@property (nonatomic, strong)NSString  *contact;

@property (nonatomic, strong)NSData    *imageData;
@property (nonatomic, strong)NSArray   *maskFrom;
@property (nonatomic, strong)NSArray   *maskTo;
@property (nonatomic, assign)CGFloat   alpha;
@property (nonatomic, assign)CGPoint   sealPointInPDF;

@property (nonatomic, strong)NSData    *ZValue;
@end

#endif

typedef NS_ENUM(NSInteger, TrustSignPDFDSReadStyle) {
    TrustSignPDFDSReadStyleSingle = 0,
    TrustSignPDFDSReadStyleSequential,
};

typedef NS_ENUM(NSInteger, TrustSignPDFDSPasswordState) {
    TrustSignPDFDSPasswordStateNoPassword       = 0,
    TrustSignPDFDSPasswordStateNeedPassword     = 1,
    TrustSignPDFDSPasswordStateVerifiedPassword = 2
};

@protocol TrustSignPDFDSDelegate <NSObject>
/*!
 @abstract  when pdf page moved call this delegate
 @param     [OUT]firstPageNo : the first active page number
 @param     [OUT]proportion : current page proportion
@param      [OUT]isDocumentEnd : wether document scroll end
 */
- (void)didPageScrolled:(NSUInteger)firstPageNo offset:(CGFloat)offset positionOffsetProportion:(CGFloat)proportion isDocumentEnd:(BOOL)isDocumentEnd;

/*!
 @abstract  when tap pdf book view call this delegate
 @param     [OUT]tapAreaInfo : the taped area info
 */
-(void)didPDFBookViewTapped:(PDFTapAreaInfo*)tapAreaInfo;

@end

@interface TrustSignPDFDS : UIView

@property (nonatomic, assign, readonly)TrustSignPDFDSPasswordState passwordState;

- (instancetype)initWithPDFPath:(NSString *)pdfPath
                          frame:(CGRect)frame
                         pageNo:(NSUInteger)pageNo
                         offset:(CGFloat)offset
                      readStyle:(TrustSignPDFDSReadStyle)readStyle
                       delegate:(id<TrustSignPDFDSDelegate>)delegate
                      errorCode:(NSInteger *)errorCode;

+ (NSString *)getVersion;

/*!
 @abstract get pdf file path
 */
- (NSString *)pdfFilePath;

/*!
 @abstract get total page number
 */
- (NSUInteger)pageCount;

/*!
 @abstract set and get current read style
 */
- (TrustSignPDFDSReadStyle)readStyle;
- (NSInteger)setReadStyle:(TrustSignPDFDSReadStyle)readStyle;

/*!
 @abstract enable and disable zoom
 */
- (BOOL)zoomEnable;
- (NSInteger)setZoomEnable:(BOOL)zoomEnable;

/*!
 @abstract set and get wether show seek bar
 */
- (BOOL)showSeekBar;
- (NSInteger)setShowSeekBar:(BOOL)showSeekBar;

/*!
 @abstract  set and get current page proportion
 */
- (CGFloat)positionOffsetProportion;
- (NSInteger)setPositionOffsetProportion:(CGFloat)positionOffsetProportion;

/*!
 @abstract   current page scale, 1.f is not zoom
 */
- (CGFloat)zoomScale;

/*!
 @abstract   verify pdf file password.
 @param      [in]pdfPassword : password to verify
 @result     [out]0 is OK, other is error
*/
- (NSInteger)verifyPDFPassword:(NSString *)pdfPassword;

/*!
 @abstract   rest zoomScale to 1.f, only when zoomEnable
 @result     [out]0 is OK, other is error
*/
- (NSInteger)resetZoomScale;

/*!
 @abstract   all active pages info
 @result     [out]all active pages info(PDFPageInfo object)
*/
- (NSArray *)activePagesInfo;

/*!
 @abstract  current first visible page info
 @param     [OUT]pageNo : first page number
 @param     [OUT]pageOffset : first page offset
 @result    0 is OK, other is error
*/
- (NSInteger)currentPageNo:(NSUInteger *)pageNo
                pageOffset:(CGFloat *)pageOffset;

/*!
 @abstract  jump specified
 @param     [OUT]pageNo : first page number
 @param     [OUT]pageOffset : first page offset
 @result    0 is OK, other is error
 */
- (NSInteger)jumpToPage:(NSUInteger)pageNo
                 offset:(CGFloat)pageOffset;

/*!
 @abstract  convert a point in bookView to point in PDF
 @param     [IN]pointInBookView : point in pdf
 @param     [OUT]pageNo : pageNo of the point in pdf
 @param     [OUT]pointInPDF : point in pdf
 @result    0 is OK, other is error
*/
- (NSInteger)convertPointInBookView:(CGPoint)pointInBookView
                             toPage:(NSUInteger *)pageNo
                         pointInPDF:(CGPoint *)pointInPDF
                          pageScale:(CGSize *)pageScale;

/*!
 @abstract  when resize pdf bookview, should re-render pdf,so can not change frame directly
 @param     [IN]newFrame : new frame for pdf bookview
 @result    0 is OK, other is error
*/
- (NSInteger)resizeFrame:(CGRect)newFrame;

#pragma mark - Verify seal functions
/*!
 @abstract  verify all seal in pdf
 @param     [IN]certChainPaths : cert chain paths to verify pdf
 @param     [IN]crlPaths : crl paths to verify pdf
 @param     [IN]verifyResultBlock : verify result callback
 @result    0 is OK, other is error
*/
- (NSInteger)verifyAllSealWithCertChainPaths:(NSArray *)certChainPaths
                                    crlPaths:(NSArray *)crlPaths
                           verifyResultBlock:(void (^)(NSInteger errorCode, NSArray *verifyResult))verifyResultBlock;
/*!
 @abstract  verify seal in pdf
 @param     [IN]pageNo : seal in which page to verify
 @param     [IN]pointInPDF : point in pdf to verify
 @param     [IN]certChainPaths : cert chain paths to verify pdf
 @param     [IN]crlPaths : crl paths to verify pdf
 @param     [IN]verifyResultBlock : verify result callback
 @result    0 is OK, other is error
*/
- (NSInteger)verifySealInPage:(NSUInteger)pageNo
                   pointInPDF:(CGPoint)pointInPDF
                certChainPath:(NSArray *)certChainPaths
                     crlPaths:(NSArray *)crlPaths
            verifyResultBlock:(void (^)(NSInteger errorCode, PDFSealSignatureInfo *verifyResult))verifyResultBlock;

#pragma mark - Seal pdf functions
/*!
 @abstract  seal at pdf
 @param     [IN]pageNo : seal to which page
 @param     [IN]sealAreaType : seal area type
 @param     [IN]pointInPDF : the point to seal in pdf
 @param     [IN]sealType : seal type(see PDFSealType)
 @param     [IN]hashType : hash type(see PDFSealCipherType)
 @param     [IN]cipherType : cipher type(see PDFSealCipherType)
 @param     [IN]sealInfo : seal info to seal in pdf
 @param     [IN]destinationPath : the destination path of sealed pdf
 @param     [OUT]signHashCallback : sign hash callback function
 @param     [OUT]sealCompleteBlock : seal complete call back
 @result    0 is OK, other is error
*/
- (NSInteger)sealPDFAtPage:(NSUInteger)pageNo
              sealAreaType:(PDFSealAreaType)sealAreaType
                pointInPDF:(CGPoint)pointInPDF
                  sealType:(PDFSealType)sealType
                  hashType:(PDFSealHashType)hashType
                cipherType:(PDFSealCipherType)cipherType
                  sealInfo:(PDFSealInfo *)sealInfo
           destinationPath:(NSString *)destinationPath
          signHashCallback:(void (^)(NSData *hashData, NSError **error, NSData **signedData))signHashCallback
         sealCompleteBlock:(void (^)(PDFSealResult sealReslt))sealCompleteBlock;

/*!
 @abstract  generate time stamp request
 @param     [IN]hashType : hash type of time stamp
 @param     [IN]sourceData : source data to generate time stamp
 @param     [OUT]timestampRequestData : the result timestamp request data
 @result    0 is OK, other is error
*/
+ (NSInteger)generateTimestampRequest:(PDFSealHashType)hashType
                           sourceData:(NSData *)sourceData
                 timestampRequestData:(NSData **)timestampRequestData;

/*!
 @abstract  encode timestamp request data
 @param     [IN]hashData : the hash data to encode timestamp request data
 @param     [IN]hashType : the hash type of hash data
 @param     [OUT]timestampRequestData : the result timestamp request data
 @result    0 is OK, other is error
*/
+ (NSInteger)encodeTimestampRequest:(NSData *)hashData
                           hashType:(PDFSealHashType)hashType
                        requestData:(NSData **)timestampRequestData;

/*!
 @abstract  update the timestamp data in pkcs7 signautre data
 @param     [IN]pkcs7ignatureData : the source pkcs7 signature data
 @param     [IN]timestampResponseData : the timestamp response data
 @param     [OUT]timestampedPKCS7SignatureData : the updated pkcs7 signature data
 @result    0 is OK, other is error
*/
+ (int)updataTimestampInPKCS7Signature:(NSData *)pkcs7ignatureData
                 timestampResponseData:(NSData *)timestampResponseData
                    pkcs7SignatureData:(NSData **)timestampedPKCS7SignatureData;

@end
