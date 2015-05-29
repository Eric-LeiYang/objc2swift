@interface MyClass()

@property (nonatomic, readwrite) BOOL loadingContentInfo;
@property NSString *privateProp;

- (void)privateMethod;

@end

@implementation MyClass

- (void)doSomething
{
    [self somethingWithArg1:@"hello" arg2:0];
    NSString formattedText = [NSString stringWithFormat:@"%@ %@", rawResult[@"FirstName"], rawResult[@"LastName"]];
    entity.access = [[NSString alloc] initWithFormat:@"%@駅から徒歩%@分", nearestStation, time];
    YSSSearchResultSet *resultSet = [[YSSSearchResultSet alloc] init];
    MyClass *myClass = [[MyClass alloc]initWithName:@"Snoopy"];
}

- (NSString *)somethingWithArg1:(id)arg1 arg2:(int)arg2
{
    return @"something";
}

+ (void)classMethod
{
    @"classMethod";
}

- (void)privateMethod
{
    @"private";
}

//- (void)funcA:(NSString *)p1 p2:(void (^)(id, NSError *))p2
//{
//}

- (NSURL *)faviconURL
{
    return _faviconURL ?: _linkURL ? [YSSURLGenerator faviconURLForURL:_linkURL] : nil;
}

@end

@implementation MyClass(Category1)

- (void)category1Method
{
}

@end
