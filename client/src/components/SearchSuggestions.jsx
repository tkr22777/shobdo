import React from 'react';

const Comment = (props) => {
    const listItems = props.suggestions.map((item, i) => {
        return (
            <li key={i}>
                <a href="#">{item}</a>
            </li>
        );
    });

    return (
        <div>
            <ul>{listItems}</ul>
        </div>
    );
};

Comment.displayName = 'Comment';
export default Comment;
