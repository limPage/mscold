import { Link } from 'react-router-dom';
import styles from './ReadQnA.module.css'
import { useParams } from 'react-router';
import { useState, useEffect, useRef } from 'react';
import DOMPurify from 'dompurify';
import axios from "axios"



function ReadQnA() {

    const params = useParams();
    const [isLoading, setIsLoading] = useState(true);
    const moment = require('moment');
    const [posts, setPosts] = useState([]);
    const [date, setDate] = useState("");
    const [writeCommentContent, setWriteCommentContent] = useState("");
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [modifiedContent, setModifiedContent] = useState("");
    const [userId, setUserId] = useState('');

    const commentInputRef = useRef(null);
    const fetchData = async () => {
        try {
            const apiUrl = `http://localhost:8080/category/readQnA?pid=${params.pid}`;
            const response = await axios.get(apiUrl);
            setPosts(response.data.data);
            setDate(moment(response.data.data.createdDate).format('YYYY-MM-DD HH:mm'));
            setIsLoading(false);

        } catch (error) {
            setIsLoading(false);
            alert("데이터 로드 중 오류가 발생했습니다. 확인 후 다시 시도해주세요.");
            console.error('데이터를 불러오는 중 에러 발생: ', error);
            window.location.href = '/category/QnA';
        }
    };


    useEffect(() => {

        setUserId(sessionStorage.getItem('sessionID')); 
     
        // 비동기 함수를 즉시 호출합니다.
        fetchData();
    }, []); // 빈 배열을 전달하여 컴포넌트가 처음 렌더링될 때만 실행되도록 합니다.

    if (isLoading) {
        return (<div className={styles.Body}>
            <div className={styles.TitleRow}>
                <span>홈 〉 게시판 〉 QnA</span>
                <h1>고객 질문 [Q&A] </h1>

            </div>

            <table>
                <thead>
                    <tr>
                        <th>

                            제목
                        </th>
                        <td><strong></strong></td>
                    </tr>
                    <tr>
                        <th>

                            종류
                        </th>

                        <td>문의</td>

                    </tr>
                    <tr className={styles.SubjectRow}>
                        <th>

                            작성자
                        </th>
                        <td></td>

                        <th>

                            작성일
                        </th>

                        <td></td>
                    </tr>

                </thead>
                <tbody>
                    <tr className={styles.Content}>

                    </tr>

                </tbody>
            </table>

        </div>)

    }

    const onDelete = async () => {
        if (!window.confirm('게시물을 삭제하시겠습니까?')) {
            if(sessionStorage.getItem("sessionID")==null || posts.member.id!==sessionStorage.getItem("sessionID") ){
                alert("로그인을 확인해주세요.");
                return window.location="../"
            }
         
            console.log(posts.id);
            return false;
        }


        try {
            const apiUrl = `http://localhost:8080/category/deleteQnA`;
            const data = {
                pid: posts.id,
                id: userId
            };

            const response = await axios.post(apiUrl, data);


            alert(response.data.message);
            window.location.href = '/category/QnA';
        } catch (error) {
            console.error('게시물 삭제 중 에러 발생: ', error);
            alert('게시물 삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
            window.location.href = '/category/QnA';
        }
       
    };

    const openComment = () => {
        const commentRow = document.getElementById("commentWriteRow");
        const commentWriteContent = document.getElementById("commentWriteContent");

        


        if (commentRow.classList.contains("visible")) {
            if (window.confirm("댓글 작성을 취소 하겠습니까?")) {
                commentRow.classList.remove("visible");
                commentRow.style.display = "none";
                setWriteCommentContent("");
                commentWriteContent.value="";
                
            }
        } else {

            commentRow.classList.add("visible");
            commentRow.style.display = "block";
        }


    }
    const onCommentWrite = async () => {

        if (sessionStorage.getItem("sessionID")==null) {
            alert("로그인을 확인해주세요.");
            // e.defaultPrevented;
            return false;
        }

        if (writeCommentContent == "") {
            alert("댓글을 입력해주세요.");
            // e.defaultPrevented;
            commentInputRef.current.focus();
            return false;
        }
        else {
            const commentRow = document.getElementById("commentWriteRow");
            const commentWriteContent = document.getElementById("commentWriteContent");
            
            const data = {
                pid: posts.id,
                id: userId,
                content: writeCommentContent,
                // cid: posts.comment.id

            }

            try {
                const response = await axios.post('http://localhost:8080/category/writeComment', data);
                if (response.status === 200) {
                    if (response.data.success == true) {

                        alert(response.data.successMessage);
                        commentRow.classList.remove("visible");
                        commentRow.style.display = "none";
                        setWriteCommentContent("");
                        commentWriteContent.value="";
                        fetchData();

                    } 
                    // 성공적으로 응답을 받았을 때의 처리

                } else {
                    // 서버 응답이 200이 아닌 경우의 처리
                    alert("업로드를 실패했습니다. 서버를 확인해주세요.");
                }
            } catch (error) {
                // 오류 발생 시의 처리
                console.error(error);
                alert("업로드 중 오류가 발생했습니다.");
            }
        };


    }
    const onWriteCommentHandler = (et) => {
        setWriteCommentContent(et.currentTarget.value);
    }
    const onModifiedContentHandler = (e) => {
        
        setModifiedContent(e.currentTarget.value);
      };
    const onDeleteComment = async (cid) => {
        if (!window.confirm('댓글을 삭제하시겠습니까?')) {
            if(sessionStorage.getItem("sessionID")==null){
                alert("로그인을 확인해주세요.");
                return false;
            }
            console.log(posts.id);
            return false;
        }


        try {
            
            const apiUrl = `http://localhost:8080/category/deleteComment`;
            const data = {
                pid: posts.id,
                id: userId,
                cid: cid
            };

            const response = await axios.post(apiUrl, data);


            alert(response.data.message);
            fetchData();
        } catch (error) {
            console.error('댓글 삭제 중 에러 발생: ', error);
            alert('댓글 삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
            window.location.href = '/category/QnA';
        }
    };

    const onModifyComment = (commentId, content) => {
        setModifiedContent(content);
        setEditingCommentId(commentId);
          };
    

    
    const onSaveModifiedComment = async (commentId, modifiedContent, commentWriter) => {

     


           if(modifiedContent.trim()==""){
            alert("수정할 댓글 내용을 작성해주세요.");

            commentInputRef.current.focus();
            
            return false;
        }
      

        const data = {
            cid:commentId,
            content: modifiedContent,
            pid: posts.id,
            id: commentWriter!=null?commentWriter: userId,
        }

    
        try {
            const response = await axios.post('http://localhost:8080/category/writeComment', data);
            if (response.status === 200) {
                if (response.data.success == true) {
               
                    alert(response.data.successMessage);
                        setEditingCommentId("");
                        fetchData();
                     
                } else {
                
                        alert(response.data.errorMessage);
                        fetchData();

                       
                }
              // 성공적으로 응답을 받았을 때의 처리
            
            } else {
              // 서버 응답이 200이 아닌 경우의 처리
              alert("업로드를 실패했습니다. 서버를 확인해주세요.");
            }
          } catch (error) {
            // 오류 발생 시의 처리
            console.error(error);
            alert("업로드 중 오류가 발생했습니다.");
          }
          };
    
   
    


    return (


        <div className={styles.Body}>
            <div className={styles.TitleRow}>
                <span>홈 〉 게시판 〉 QnA</span>
                <h1>고객 질문 [Q&A] </h1>

            </div>

            <table>
                <thead>
                    <tr>
                        <th>

                            제목
                        </th>
                        <td><strong>{posts.title}</strong></td>
                    </tr>
                    <tr className={styles.SubjectRow}>
                    <th >

                        번호
                        </th>
                        <td>{posts.id}</td>

                        <th>

                            종류
                        </th>

                        <td>{posts.subject}문의</td>

                        
                    </tr>
                    <tr className={styles.SubjectRow}>
                        <th>

                            작성자
                        </th>
                        <td>{posts.member.id}</td>

                        <th>

                            작성일
                        </th>

                        {/* <td>{date}</td> */}
                        <td>{moment(posts.createdDate).format('YYYY-MM-DD HH:mm')}</td>

                    </tr>

                </thead>
                <tbody>
                    <tr className={styles.Content}>
                        <td id='content' dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(posts.content) }}>

                        </td>
                    </tr>

                </tbody>


            </table>


            <div className={styles.CommentContainer}>
                
            {posts.comments && posts.comments.length > 0 ? (
                <ul className={styles.CommentRow}>
                    {posts.comments.map(comment => (
                         <li key={comment.id} className={comment.writer === userId ? styles.MyComment:comment.writer === "cold" ?styles.admin:"" }>
                        <span className={styles.CommentWriter} >{comment.writer}</span>
                        {editingCommentId === comment.id ? (
                           
                                <input
                                    className={styles.CommentModifyContent}
                                    type='text'
                                    maxLength={100}
                                    defaultValue={comment.content}
                                    onChange={onModifiedContentHandler} ref={commentInputRef}
                                />
                        
                        ) : (
                            <span className={styles.CommentContent}>{comment.content}</span>
                            
                        )}
                        <span className={styles.CommentCreatedDate}>{moment(comment.createdDate).format('YY/MM/DD HH:mm')}</span>

                        {userId == comment.writer &&(
                            <>
                        {editingCommentId !== comment.id && (
                            <>
                            <button onClick={() => onModifyComment(comment.id, comment.content)}>수정</button>
                            <button onClick={() => onDeleteComment(comment.id)}>삭제</button>
                            </>
                        )}
                        {editingCommentId === comment.id ? (
                            <>
                                
                                <button onClick={() => onSaveModifiedComment(comment.id, modifiedContent, comment.writer)}>저장</button>
                                <button onClick={() => setEditingCommentId(null)}>취소</button>

                            </>
                            
                        ) : (
                            <></>
                            
                        )} </>)
                      
                        
                        
                        }
                        
                    </li>
                    ))}
                </ul>
            ) : null}
                    


                
                <ul className={styles.CommentWriteRow} id="commentWriteRow">
                    <li>
                        <   span> {userId}</span>
                        <input type='text' id='commentWriteContent' placeholder='100자 이내로 댓글을 작성해주세요.' maxLength={100} onChange={onWriteCommentHandler} ref={commentInputRef}></input>
                        <button type='button' onClick={onCommentWrite}>등록</button>
                        <button type='button' onClick={openComment}>취소</button>
                    </li>
                </ul>

            </div>



            <div className={styles.ButtonRow}>
                <Link to="/category/QnA" id="goListButton">목  록</Link>
                <span></span>
                {userId !== null && userId !== '' && (
                <a onClick={openComment}>댓글달기</a>

                  )}
                
                {userId == posts.member.id &&(
                        <>
                <Link to={"/category/qna/Modify/" + params.pid} id="modifyButton">수  정</Link>
                <Link id="deleteButton" onClick={onDelete}>삭  제</Link>
                </>
                )}

            </div>
        </div>
    )
}



export default ReadQnA;