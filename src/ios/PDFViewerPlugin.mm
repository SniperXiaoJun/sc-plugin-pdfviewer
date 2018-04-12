//
//  PDFViewerPlugin.m
//  PDFViewer
//
//  Created by mr、j on 2018/4/11.
//

#import "PDFViewerPlugin.h"
#import "TrustSignPDFDS.h"

@interface PDFViewerPlugin ()

@property (nonatomic, strong) TrustSignPDFDS *pdfView;

@end

@implementation PDFViewerPlugin

-(void)viewpdf:(CDVInvokedUrlCommand *)command
{
    NSData *data = [[NSData alloc] initWithBase64EncodedString:command.arguments[0] options:0];
    NSString *documentsPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *strPath = [documentsPath stringByAppendingPathComponent:@"loanAgreement.pdf"];
    [data writeToFile:strPath atomically:YES];
    NSString *string = [documentsPath stringByAppendingString:@"/loanAgreement.pdf"];
    [self initPDFViewWithFilePath:string];
}

-(void)initPDFViewWithFilePath:(NSString *)filePath
{
    NSUInteger pageNo = 0;
    CGFloat pageOffset = 0.f;
    NSInteger errorCode = 0;
    self.pdfView = [[TrustSignPDFDS alloc] initWithPDFPath:filePath frame:self.viewController.view.frame pageNo:pageNo offset:pageOffset readStyle:TrustSignPDFDSReadStyleSequential delegate:nil errorCode:&errorCode];
    [self.viewController.view addSubview:self.pdfView];
}

@end
