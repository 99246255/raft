package com.enumaelish.grpc.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.14.0)",
    comments = "Source: raft.proto")
public final class RaftServiceGrpc {

  private RaftServiceGrpc() {}

  public static final String SERVICE_NAME = "raft.RaftService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
      com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getPrevoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "prevote",
      requestType = com.enumaelish.grpc.proto.RpcMessage.VoteRequest.class,
      responseType = com.enumaelish.grpc.proto.RpcMessage.VoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
      com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getPrevoteMethod() {
    io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest, com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getPrevoteMethod;
    if ((getPrevoteMethod = RaftServiceGrpc.getPrevoteMethod) == null) {
      synchronized (RaftServiceGrpc.class) {
        if ((getPrevoteMethod = RaftServiceGrpc.getPrevoteMethod) == null) {
          RaftServiceGrpc.getPrevoteMethod = getPrevoteMethod = 
              io.grpc.MethodDescriptor.<com.enumaelish.grpc.proto.RpcMessage.VoteRequest, com.enumaelish.grpc.proto.RpcMessage.VoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "raft.RaftService", "prevote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.VoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.VoteResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RaftServiceMethodDescriptorSupplier("prevote"))
                  .build();
          }
        }
     }
     return getPrevoteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
      com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getVoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "vote",
      requestType = com.enumaelish.grpc.proto.RpcMessage.VoteRequest.class,
      responseType = com.enumaelish.grpc.proto.RpcMessage.VoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
      com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getVoteMethod() {
    io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.VoteRequest, com.enumaelish.grpc.proto.RpcMessage.VoteResponse> getVoteMethod;
    if ((getVoteMethod = RaftServiceGrpc.getVoteMethod) == null) {
      synchronized (RaftServiceGrpc.class) {
        if ((getVoteMethod = RaftServiceGrpc.getVoteMethod) == null) {
          RaftServiceGrpc.getVoteMethod = getVoteMethod = 
              io.grpc.MethodDescriptor.<com.enumaelish.grpc.proto.RpcMessage.VoteRequest, com.enumaelish.grpc.proto.RpcMessage.VoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "raft.RaftService", "vote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.VoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.VoteResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RaftServiceMethodDescriptorSupplier("vote"))
                  .build();
          }
        }
     }
     return getVoteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest,
      com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> getAppendEntriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "appendEntries",
      requestType = com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest.class,
      responseType = com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest,
      com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> getAppendEntriesMethod() {
    io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest, com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> getAppendEntriesMethod;
    if ((getAppendEntriesMethod = RaftServiceGrpc.getAppendEntriesMethod) == null) {
      synchronized (RaftServiceGrpc.class) {
        if ((getAppendEntriesMethod = RaftServiceGrpc.getAppendEntriesMethod) == null) {
          RaftServiceGrpc.getAppendEntriesMethod = getAppendEntriesMethod = 
              io.grpc.MethodDescriptor.<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest, com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "raft.RaftService", "appendEntries"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RaftServiceMethodDescriptorSupplier("appendEntries"))
                  .build();
          }
        }
     }
     return getAppendEntriesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest,
      com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> getInstallSnapshotMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "installSnapshot",
      requestType = com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest.class,
      responseType = com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest,
      com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> getInstallSnapshotMethod() {
    io.grpc.MethodDescriptor<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest, com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> getInstallSnapshotMethod;
    if ((getInstallSnapshotMethod = RaftServiceGrpc.getInstallSnapshotMethod) == null) {
      synchronized (RaftServiceGrpc.class) {
        if ((getInstallSnapshotMethod = RaftServiceGrpc.getInstallSnapshotMethod) == null) {
          RaftServiceGrpc.getInstallSnapshotMethod = getInstallSnapshotMethod = 
              io.grpc.MethodDescriptor.<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest, com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "raft.RaftService", "installSnapshot"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new RaftServiceMethodDescriptorSupplier("installSnapshot"))
                  .build();
          }
        }
     }
     return getInstallSnapshotMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RaftServiceStub newStub(io.grpc.Channel channel) {
    return new RaftServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RaftServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new RaftServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RaftServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new RaftServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class RaftServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * 预投票，变候选人前，超过半数才能变成候选人
     * </pre>
     */
    public void prevote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPrevoteMethod(), responseObserver);
    }

    /**
     * <pre>
     *投票rpc
     * </pre>
     */
    public void vote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getVoteMethod(), responseObserver);
    }

    /**
     * <pre>
     *附加日志/心跳rpc
     * </pre>
     */
    public void appendEntries(com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAppendEntriesMethod(), responseObserver);
    }

    /**
     * <pre>
     *快照rpc
     * </pre>
     */
    public void installSnapshot(com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getInstallSnapshotMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPrevoteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
                com.enumaelish.grpc.proto.RpcMessage.VoteResponse>(
                  this, METHODID_PREVOTE)))
          .addMethod(
            getVoteMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.enumaelish.grpc.proto.RpcMessage.VoteRequest,
                com.enumaelish.grpc.proto.RpcMessage.VoteResponse>(
                  this, METHODID_VOTE)))
          .addMethod(
            getAppendEntriesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest,
                com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse>(
                  this, METHODID_APPEND_ENTRIES)))
          .addMethod(
            getInstallSnapshotMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest,
                com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse>(
                  this, METHODID_INSTALL_SNAPSHOT)))
          .build();
    }
  }

  /**
   */
  public static final class RaftServiceStub extends io.grpc.stub.AbstractStub<RaftServiceStub> {
    private RaftServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RaftServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RaftServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * 预投票，变候选人前，超过半数才能变成候选人
     * </pre>
     */
    public void prevote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPrevoteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *投票rpc
     * </pre>
     */
    public void vote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getVoteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *附加日志/心跳rpc
     * </pre>
     */
    public void appendEntries(com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *快照rpc
     * </pre>
     */
    public void installSnapshot(com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest request,
        io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getInstallSnapshotMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RaftServiceBlockingStub extends io.grpc.stub.AbstractStub<RaftServiceBlockingStub> {
    private RaftServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RaftServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RaftServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * 预投票，变候选人前，超过半数才能变成候选人
     * </pre>
     */
    public com.enumaelish.grpc.proto.RpcMessage.VoteResponse prevote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request) {
      return blockingUnaryCall(
          getChannel(), getPrevoteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *投票rpc
     * </pre>
     */
    public com.enumaelish.grpc.proto.RpcMessage.VoteResponse vote(com.enumaelish.grpc.proto.RpcMessage.VoteRequest request) {
      return blockingUnaryCall(
          getChannel(), getVoteMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *附加日志/心跳rpc
     * </pre>
     */
    public com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse appendEntries(com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request) {
      return blockingUnaryCall(
          getChannel(), getAppendEntriesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     *快照rpc
     * </pre>
     */
    public com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse installSnapshot(com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest request) {
      return blockingUnaryCall(
          getChannel(), getInstallSnapshotMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RaftServiceFutureStub extends io.grpc.stub.AbstractStub<RaftServiceFutureStub> {
    private RaftServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private RaftServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new RaftServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * 预投票，变候选人前，超过半数才能变成候选人
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> prevote(
        com.enumaelish.grpc.proto.RpcMessage.VoteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getPrevoteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *投票rpc
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.enumaelish.grpc.proto.RpcMessage.VoteResponse> vote(
        com.enumaelish.grpc.proto.RpcMessage.VoteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getVoteMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *附加日志/心跳rpc
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse> appendEntries(
        com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     *快照rpc
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse> installSnapshot(
        com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getInstallSnapshotMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PREVOTE = 0;
  private static final int METHODID_VOTE = 1;
  private static final int METHODID_APPEND_ENTRIES = 2;
  private static final int METHODID_INSTALL_SNAPSHOT = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RaftServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RaftServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PREVOTE:
          serviceImpl.prevote((com.enumaelish.grpc.proto.RpcMessage.VoteRequest) request,
              (io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse>) responseObserver);
          break;
        case METHODID_VOTE:
          serviceImpl.vote((com.enumaelish.grpc.proto.RpcMessage.VoteRequest) request,
              (io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.VoteResponse>) responseObserver);
          break;
        case METHODID_APPEND_ENTRIES:
          serviceImpl.appendEntries((com.enumaelish.grpc.proto.RpcMessage.AppendEntriesRequest) request,
              (io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.AppendEntriesResponse>) responseObserver);
          break;
        case METHODID_INSTALL_SNAPSHOT:
          serviceImpl.installSnapshot((com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotRequest) request,
              (io.grpc.stub.StreamObserver<com.enumaelish.grpc.proto.RpcMessage.InstallSnapshotResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RaftServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RaftServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.enumaelish.grpc.proto.RpcMessage.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("RaftService");
    }
  }

  private static final class RaftServiceFileDescriptorSupplier
      extends RaftServiceBaseDescriptorSupplier {
    RaftServiceFileDescriptorSupplier() {}
  }

  private static final class RaftServiceMethodDescriptorSupplier
      extends RaftServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RaftServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RaftServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RaftServiceFileDescriptorSupplier())
              .addMethod(getPrevoteMethod())
              .addMethod(getVoteMethod())
              .addMethod(getAppendEntriesMethod())
              .addMethod(getInstallSnapshotMethod())
              .build();
        }
      }
    }
    return result;
  }
}
